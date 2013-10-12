package jevamap;

import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.config.VariableValue;
import jeva.game.ControlledCamera;
import jeva.game.ResourceLoadingException;
import jeva.game.IWorldCamera;
import jeva.graphics.Sprite;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.IInteractable;
import jeva.world.World;
import jeva.world.WorldDirection;
import jeva.world.WorldLayer;
import jevarpg.RpgCharacter;
import jevarpg.RpgGame;

public class MapEditor extends RpgGame implements IEditorPaneListener
{
	private EditorPane m_pane = new EditorPane(this);

	private int m_selectedLayer;
	private String m_nullTile;

	private ControlledCamera m_camera = new ControlledCamera();
	private Vector2F m_cameraMovement = new Vector2F();

	private String m_worldScript = "";

	@Override
	protected void startup()
	{
		super.startup();
		m_pane.setVisible(true);
		m_selectedLayer = -1;
	}

	@Override
	public synchronized void update(int deltaTime)
	{
		if (!m_cameraMovement.isZero())
			m_camera.move(m_cameraMovement.normalize().multiply(0.3F));

		if (getWorld() != null)
			getWorld().update(deltaTime);

		super.update(deltaTime);
	}

	@Override
	public synchronized void initializeWorld(int worldWidth, int worldHeight, int tileWidth, int tileHeight)
	{
		World w = new World(getEntityLibrary(), worldWidth, worldHeight, tileWidth, tileHeight, 0);
		setWorld(w);

		WorldLayer mainLayer = new WorldLayer();

		Sprite nullTile = Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(m_nullTile)));

		for (int y = 0; y < worldHeight; y++)
		{
			for (int x = 0; x < worldWidth; x++)
			{
				EditorTile editorTile = new EditorTile(nullTile, WorldDirection.Zero, "idle", true, true, false, 1.0F);
				editorTile.setLocation(new Vector2D(x, y));
				editorTile.addToWorld(getWorld(), mainLayer);
			}
		}

		m_selectedLayer = 0;
		w.addLayer(mainLayer);
		m_pane.setMapLayers(1);
	}

	@Override
	public synchronized void setTile(EditorTile tile, boolean isTraversable, boolean isStatic, String sprite, String animation, WorldDirection direction, float fVisibility, boolean enableSplitting)
	{
		tile.setTraversable(isTraversable);
		tile.setStatic(isStatic);

		if (sprite.length() != 0 && sprite.compareTo(m_nullTile) != 0)
			tile.setSpriteName(sprite);
		else
			tile.setSpriteName(m_nullTile);

		tile.setDirection(direction);
		tile.setSpriteAnimation(animation);
		tile.setVisibilityObstruction(fVisibility);
		tile.setEnableSplitting(enableSplitting);
	}

	@Override
	public void worldSelection(InputMouseEvent e, Vector2D location)
	{
		if (m_selectedLayer >= 0 && m_selectedLayer < getWorld().getLayers().length)
		{
			ArrayList<IInteractable> interactables = getWorld().getLayers()[m_selectedLayer].getTileEffects(location).interactables;

			EditorTile selectedTile = null;

			for (IInteractable i : interactables)
			{
				if (i instanceof EditorTile)
				{
					selectedTile = (EditorTile) i;
					break;
				}
			}

			if (selectedTile == null)
			{
				selectedTile = new EditorTile(m_nullTile, WorldDirection.Zero, "idle", true, true, false, 1.0F);
				selectedTile.setLocation(location);
				selectedTile.addToWorld(getWorld(), getWorld().getLayers()[m_selectedLayer]);
			}

			m_pane.selectedTile(selectedTile);
		}
	}

	@Override
	public synchronized void refreshEntity(EditorEntity entity)
	{
		if (entity != null)
			entity.refresh(getWorld());
	}

	@Override
	public void removeEntity(EditorEntity entity)
	{
		entity.remove(getWorld());
	}

	private static void initEntityProperties(EditorEntity entity, Variable srcProperty, String parentName)
	{
		if (!srcProperty.getValue().getString().isEmpty())
			entity.setPostInitAssignment(parentName, srcProperty.getValue().getString());

		for (Variable var : srcProperty)
			initEntityProperties(entity, var, parentName + Variable.NAME_SPLIT + var.getName());
	}

	private static void initEntityProperties(EditorEntity entity, Variable srcProperty)
	{
		initEntityProperties(entity, srcProperty, srcProperty.getName());
	}

	@Override
	public synchronized void setEntityLayer(int layer)
	{
		if (getWorld() != null)
			getWorld().setEntityLayer(layer);
	}

	@Override
	public synchronized void openMap(VariableStore source)
	{
		m_pane.clearEntities();

		World world = new World(getEntityLibrary(), source.getVariable("width").getValue().getInt(), source.getVariable("height").getValue().getInt(), source.getVariable("tileWidth").getValue().getInt(), source.getVariable("tileHeight").getValue().getInt(), source.getVariable("entityLayer").getValue().getInt());

		if (source.variableExists("entity"))
		{
			for (Variable entityVar : source.getVariable("entity"))
			{
				// Entity entity =
				// Entity.create(entityVar.getValue().getObjectName(),
				// entityVar.getName(),
				// Arrays.asList(entityVar.getValue().getObjectArguments()));
				EditorEntity entity = new EditorEntity(entityVar.getName(), entityVar.getValue().getObjectName(), new VariableValue(entityVar.getValue().getObjectArguments()).getString());

				// Assign post-init assignments
				if (source.variableExists(entity.getName()))
				{
					for (Variable varProperty : source.getVariable(entity.getName()))
					{
						initEntityProperties(entity, varProperty);
					}
				}

				m_pane.addEntity(entity);

				entity.refresh(world);
			}
		}

		// Construct Map Layers
		Variable[] tileDeclarations = source.getVariable("tile").getVariableArray();

		Variable[] layerDeclarations = source.getVariable("layer").getVariableArray();

		for (int i = 0; i < layerDeclarations.length; i++)
		{
			WorldLayer worldLayer = new WorldLayer();

			Integer[] tileDeclIndices = layerDeclarations[i].getValue().getIntArray();

			for (int tileIndex = 0; tileIndex < tileDeclIndices.length; tileIndex++)
			{
				if (tileDeclIndices[tileIndex] >= tileDeclarations.length)
					throw new ResourceLoadingException("Undeclared Tile Class Index Used");

				EditorTile tile;

				if (tileDeclIndices[tileIndex] >= 0)
				{
					VariableValue[] arguments = tileDeclarations[tileDeclIndices[tileIndex]].getValue().getObjectArguments();

					if (arguments.length < 6)
						throw new ResourceLoadingException("Illegal number of arguments for tile.");

					boolean enablesSplitting = false;

					if (arguments.length >= 7)
						enablesSplitting = arguments[6].getBoolean();

					tile = new EditorTile(arguments[0].getString(), WorldDirection.values()[arguments[1].getInt()], arguments[2].getString(), arguments[3].getBoolean(), arguments[4].getBoolean(), enablesSplitting, arguments[5].getFloat());
				} else
					tile = new EditorTile(m_nullTile, WorldDirection.Zero, "idle", true, true, false, 1.0F);

				tile.setLocation(new Vector2D(tileIndex % world.getWidth(), (int) Math.floor(tileIndex / world.getHeight())));

				tile.addToWorld(world, worldLayer);
			}

			world.addLayer(worldLayer);
		}

		if (source.variableExists("script"))
		{
			m_worldScript = source.getVariable("script").getValue().getString();
			m_pane.setScript(m_worldScript);
		}

		if (layerDeclarations.length > 0)
			m_selectedLayer = 0;
		else
			m_selectedLayer = -1;

		m_pane.setMapLayers(layerDeclarations.length);
		m_pane.setEntityLayer(world.getEntityLayer());
		setWorld(world);
	}

	@Override
	public synchronized void saveMap(FileOutputStream fileOutputStream, ArrayList<EditorEntity> entities)
	{
		VariableStore var = new VariableStore();

		var.setVariable("width", new VariableValue(getWorld().getWidth()));
		var.setVariable("height", new VariableValue(getWorld().getHeight()));
		var.setVariable("tileWidth", new VariableValue(getWorld().getTileWidth()));
		var.setVariable("tileHeight", new VariableValue(getWorld().getTileHeight()));
		var.setVariable("entityLayer", new VariableValue(getWorld().getEntityLayer()));

		Variable varTiles = var.setVariable("tile", new VariableValue());
		Variable varLayers = var.setVariable("layer", new VariableValue());
		Variable varEntities = var.setVariable("entity", new VariableValue());

		ArrayList<EditorTile> tiles = new ArrayList<EditorTile>();

		ArrayList<Integer[]> layers = new ArrayList<Integer[]>();

		for (WorldLayer layer : getWorld().getLayers())
		{
			layers.add(new Integer[getWorld().getWidth() * getWorld().getHeight()]);

			for (int y = 0; y < getWorld().getHeight(); y++)
			{
				for (int x = 0; x < getWorld().getWidth(); x++)
				{
					ArrayList<IInteractable> interactables = layer.getTileEffects(new Vector2D(x, y)).interactables;

					EditorTile editorTile = null;

					for (IInteractable i : interactables)
					{
						if (i instanceof EditorTile && ((EditorTile) i).getSpriteName().compareTo(m_nullTile) != 0 && ((EditorTile) i).getSpriteName().length() > 0)
						{
							editorTile = (EditorTile) i;
							break;
						}
					}

					if (editorTile != null && !tiles.contains(editorTile))
						tiles.add(editorTile);

					int tileIndex = (editorTile == null ? -1 : tiles.indexOf(editorTile));

					layers.get(layers.size() - 1)[x + y * getWorld().getWidth()] = tileIndex;
				}
			}
		}

		for (int i = 0; i < tiles.size(); i++)
		{
			varTiles.setVariable(String.valueOf(i), new VariableValue(new VariableValue(tiles.get(i).getSpriteName()), new VariableValue(tiles.get(i).getDirection().ordinal()), new VariableValue(tiles.get(i).getSpriteAnimation()), new VariableValue(tiles.get(i).isTraversable()), new VariableValue(tiles.get(i).isStatic()), new VariableValue(tiles.get(i).getVisibilityObstruction()), new VariableValue(tiles.get(i).enablesSplitting())));
		}

		for (int i = 0; i < layers.size(); i++)
		{
			varLayers.setVariable(String.valueOf(i), new VariableValue(layers.get(i)));
		}

		for (EditorEntity e : entities)
		{
			varEntities.setVariable(e.getName(), new VariableValue(e.getClassName() + e.getArguments()));

			for (Map.Entry<String, String> assignment : e.getPostInitAssignments())
			{
				var.setVariable(e.getName() + "/" + assignment.getKey(), new VariableValue(assignment.getValue()));
			}
		}

		if (m_worldScript != null && m_worldScript.length() != 0)
			var.setVariable("script", new VariableValue(m_worldScript));

		var.serialize(fileOutputStream);
	}

	@Override
	public RpgCharacter getPlayer()
	{
		return null;
	}

	@Override
	protected void onLoadedWorld()
	{
		m_camera.attach(getWorld());
	}

	@Override
	protected IWorldCamera getCamera()
	{
		return m_camera;
	}

	@Override
	public synchronized void selectLayer(int i)
	{
		m_selectedLayer = i;
	}

	@Override
	public synchronized void deleteSelectedLayer()
	{
		if (getWorld().getLayers().length > 1)
		{
			getWorld().removeLayer(getWorld().getLayers()[m_selectedLayer]);
			m_selectedLayer--;
			m_pane.setMapLayers(getWorld().getLayers().length);
		}
	}

	@Override
	public synchronized void createNewLayer()
	{
		getWorld().addLayer(new WorldLayer());
		m_pane.setMapLayers(getWorld().getLayers().length);
	}

	@Override
	public synchronized void setNullSprite(String name)
	{
		m_nullTile = name;
	}

	@Override
	public synchronized void applyScript(String script)
	{
		m_worldScript = script;

	}

	@Override
	public void keyDown(InputKeyEvent e)
	{
		switch (e.keyCode)
		{
		case KeyEvent.VK_UP:
			m_cameraMovement.y = -1;
			break;
		case KeyEvent.VK_RIGHT:
			m_cameraMovement.x = 1;
			break;
		case KeyEvent.VK_DOWN:
			m_cameraMovement.y = 1;
			break;
		case KeyEvent.VK_LEFT:
			m_cameraMovement.x = -1;
			break;
		}
	}

	@Override
	public void keyUp(InputKeyEvent e)
	{
		switch (e.keyCode)
		{
		case KeyEvent.VK_UP:
		case KeyEvent.VK_DOWN:
			m_cameraMovement.y = 0;
			break;
		case KeyEvent.VK_RIGHT:
		case KeyEvent.VK_LEFT:
			m_cameraMovement.x = 0;
			break;
		}
	}

	@Override
	public void mouseButtonStateChanged(InputMouseEvent e)
	{
	}
}