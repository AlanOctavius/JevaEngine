/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.world;

import io.github.jevaengine.CoreScriptException;
import javax.script.ScriptException;
import org.mozilla.javascript.Function;

/**
 *
 * @author Jeremy
 */
public class InvokeScriptFunctionTask extends SynchronousOneShotTask
{
	private Function m_function;
	
	private Object[] m_arguments;
	
	public InvokeScriptFunctionTask(Function function, Object ... arguments)
	{
		m_function = function;
		m_arguments = arguments;
	}
	
	@Override
	public void run(Entity entity)
	{
		try
		{
			entity.getScript().invokeScriptFunction(m_function, m_arguments);
		} catch (NoSuchMethodException | ScriptException ex)
		{
			throw new CoreScriptException(ex);
		}
	}
	
}
