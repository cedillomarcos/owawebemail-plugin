package org.jenkinsci.plugins.owaplugin.content;

import hudson.Extension;
import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.io.IOException;
import java.util.Map;

import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

@Extension
public class EmailToTokenMacro extends DataBoundTokenMacro {

    public static final String MACRO_NAME = "TO_ADD";
    
    @Override
    public boolean acceptsMacroName(String macroName) {
        return macroName.equals(MACRO_NAME);
    }

    @Override
    public String evaluate(AbstractBuild<?, ?> build, TaskListener listener, String macroName)
            throws MacroEvaluationException, IOException, InterruptedException {
                 
        Map<String, String> env = build.getEnvironment(listener);
        String value = env.get(macroName);
        if(value==null){
        	return null;
        }
        return value; 
    }

}
