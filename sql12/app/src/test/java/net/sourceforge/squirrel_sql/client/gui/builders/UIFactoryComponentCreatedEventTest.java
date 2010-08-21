/*
 * Copyright (C) 2008 Rob Manning
 * manningr@users.sourceforge.net
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sourceforge.squirrel_sql.client.gui.builders;


import java.awt.Component;

import javax.swing.JPanel;

import net.sourceforge.squirrel_sql.AbstractSerializableTest;

import org.junit.Before;

public class UIFactoryComponentCreatedEventTest extends AbstractSerializableTest
{

	Component component = new JPanel();
	UIFactory mockFactory = mockHelper.createMock(UIFactory.class);
	
	@Before
	public void setUp() throws Exception
	{
		super.serializableToTest = new UIFactoryComponentCreatedEvent(mockFactory, component);
	}


}
