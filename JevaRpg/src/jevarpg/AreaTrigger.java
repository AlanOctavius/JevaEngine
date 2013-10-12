package jevarpg;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.script.ScriptException;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.CoreScriptException;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.config.VariableValue;
import jeva.world.Actor;
import jeva.world.DialogicalEntity;
import jeva.world.EffectMap;
import jeva.world.EntityInstantiationException;
import jeva.world.RectangleSearchFilter;

public class AreaTrigger extends DialogicalEntity
{
    private static final int SCAN_INTERVAL = 400;

    private Rectangle2D.Float m_bounds;

    private int m_lastScan;

    private TriggerScript m_script = new TriggerScript();

    private ArrayList<RpgCharacter> m_includedEntities = new ArrayList<RpgCharacter>();

    public AreaTrigger(@Nullable String name, List<VariableValue> arguments)
    {
        super(name, initTriggerVariable(arguments), new AreaTriggerBridge<>());

        m_bounds = arguments.get(1).getRectangleFloat();
    }

    private static Variable initTriggerVariable(List<VariableValue> arguments)
    {
        if (arguments.size() < 2)
            throw new EntityInstantiationException("Illegal number of arguments");

        return VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(arguments.get(0).getString()));
    }

    public static class AreaTriggerBridge<T extends AreaTrigger> extends DialogicalBridge<T>
    {

    }

    @Override
    protected Variable[] getChildren()
    {
        return new Variable[]
        {};
    }

    @Override
    protected Variable setChild(String name, VariableValue value)
    {
        throw new NoSuchElementException();
    }

    @Override
    public void doLogic(int deltaTime)
    {
        m_lastScan -= deltaTime;

        if (m_lastScan <= 0)
        {
            m_lastScan = SCAN_INTERVAL;

            Actor[] entities = getWorld().getActors(new RectangleSearchFilter<Actor>(m_bounds));

            ArrayList<RpgCharacter> unfoundCharacters = new ArrayList<RpgCharacter>(m_includedEntities);

            for (Actor actor : entities)
            {
                if (!(actor instanceof RpgCharacter))
                    continue;

                RpgCharacter character = (RpgCharacter) actor;

                if (!unfoundCharacters.contains(character))
                {
                    m_includedEntities.add(character);
                    m_script.onTrigger(true, character);
                    character.addObserver(new TriggerCharacterObserver(character));
                } else
                {
                    unfoundCharacters.remove(character);
                }
            }

            for (RpgCharacter character : unfoundCharacters)
            {
                m_includedEntities.remove(character);
                m_script.onTrigger(false, character);
            }
        }
    }

    @Override
    public void blendEffectMap(EffectMap globalEffectMap)
    {
    }

    private class TriggerScript
    {
        private void onTrigger(boolean isOver, RpgCharacter character)
        {
            try
            {
                getScript().invokeScriptFunction("onTrigger", character.getScriptBridge(), isOver);
            } catch (NoSuchMethodException e)
            {
            } catch (ScriptException e)
            {
                throw new CoreScriptException("Error invoking script routine onEnterTrigger " + e.getMessage());
            }
        }
    }

    private class TriggerCharacterObserver implements IEntityObserver
    {
        private RpgCharacter m_observee;

        public TriggerCharacterObserver(RpgCharacter observee)
        {
            m_observee = observee;
        }

        @Override
        public void leaveWorld()
        {
            m_includedEntities.remove(m_observee);
            m_observee.removeObserver(this);
        }

        @Override
        public void enterWorld()
        {
        }

        @Override
        public void taskBusyState(boolean isBusy)
        {
        }
    }
}