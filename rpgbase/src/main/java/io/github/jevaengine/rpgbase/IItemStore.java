/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.rpgbase.Item.ItemIdentifer;
import io.github.jevaengine.util.Nullable;

public interface IItemStore
{
	ItemSlot[] getSlots();
	boolean hasItem(ItemIdentifer item);
	boolean addItem(ItemIdentifer item);
	boolean removeItem(ItemIdentifer item);
	boolean isFull();
	@Nullable ItemSlot getEmptySlot();
}
