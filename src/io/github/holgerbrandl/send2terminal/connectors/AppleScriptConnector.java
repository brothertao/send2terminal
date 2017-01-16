/*
 * Copyright 2011 Holger Brandl
 *
 * This code is licensed under BSD. For details see
 * http://www.opensource.org/licenses/bsd-license.php
 */

package io.github.holgerbrandl.send2terminal.connectors;


import io.github.holgerbrandl.send2terminal.Utils;
import io.github.holgerbrandl.send2terminal.settings.S2TSettings;

import java.io.IOException;


/**
 * A connector using apple script
 *
 * @author Holger Brandl
 */
public class AppleScriptConnector implements CodeLaunchConnector {

    public static void main(String[] args) {
        new AppleScriptConnector().submitCode("write.table(head(iris), file=\"~/Desktop/iris.txt\", sep=\"\\t\")\n", true);
    }


    @Override
    public void submitCode(String rCommands, boolean switchFocus2R) {
        try {

            if (Utils.isMacOSX()) {
                Runtime runtime = Runtime.getRuntime();

                String dquotesExpandedText = rCommands.replace("\\", "\\\\");
                dquotesExpandedText = dquotesExpandedText.replace("\"", "\\\"");

                // trim to remove tailing newline for blocks and especially line evaluation
                dquotesExpandedText = dquotesExpandedText.trim();


                String evalTarget = S2TSettings.getInstance().codeSnippetEvalTarget;
//                String evalTarget = "R64";

//                http://stackoverflow.com/questions/1870270/sending-commands-and-strings-to-terminal-app-with-applescript

                String evalSelection;
                if (evalTarget.equals("Terminal")) {
                    evalSelection = "tell application \"" + "Terminal" + "\" to do script \"" + dquotesExpandedText + "\" in window 0";

                    if (switchFocus2R) {
                        evalSelection = "tell application \"Terminal\" to activate\n" + evalSelection;
                    }

                } else if (evalTarget.equals("iTerm")) {
                    evalSelection = "tell application \"iTerm\" to tell current session of current terminal  to write text  \"" + dquotesExpandedText + "\"";
                    if (switchFocus2R) {
                        evalSelection = "tell application \"iTerm\" to activate\n" + evalSelection;
                    }

                } else {
                    if (switchFocus2R) {
                        evalSelection = "tell application \"" + evalTarget + "\" to activate\n" +
                                "tell application \"" + evalTarget + "\" to cmd \"" + dquotesExpandedText + "\"";
                    } else {
                        evalSelection = "tell application \"" + evalTarget + "\" to cmd \"" + dquotesExpandedText + "\"";
                    }
                }

                String[] args = {"osascript", "-e", evalSelection};

                runtime.exec(args);
            }
        } catch (IOException e1) {
            ConnectorUtils.log.error(e1);
        }
    }
}
