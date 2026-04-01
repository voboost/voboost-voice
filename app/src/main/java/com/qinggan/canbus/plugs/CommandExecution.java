package com.qinggan.canbus.plugs;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/* loaded from: classes3.dex */
public class CommandExecution {
    public static final String COMMAND_EXIT = "exit\n";
    public static final String COMMAND_LINE_END = "\n";
    public static final String COMMAND_SH = "sh";
    public static final String COMMAND_SU = "su";
    public static final String TAG = "CommandExecution";

    public static class CommandResult {
        public String errorMsg;
        public int result = -1;
        public String successMsg;
    }

    public static CommandResult execCommand(String command, boolean isRoot) {
        String[] commands = {command};
        try {
            return execCommand(commands, isRoot);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:34:0x00e0 A[PHI: r3
  0x00e0: PHI (r3v5 'process' java.lang.Process) = (r3v2 'process' java.lang.Process), (r3v3 'process' java.lang.Process), (r3v6 'process' java.lang.Process) binds: [B:77:0x014b, B:56:0x0118, B:33:0x00de] A[DONT_GENERATE, DONT_INLINE]] */
    /* JADX WARN: Removed duplicated region for block: B:95:0x0173  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static CommandResult execCommand(String[] commands, boolean isRoot) throws IOException {
        CommandResult commandResult = new CommandResult();
        if (commands == null || commands.length == 0) {
            return commandResult;
        }
        Process process = null;
        DataOutputStream os = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        try {
            try {
                try {
                    process = Runtime.getRuntime().exec(isRoot ? COMMAND_SU : COMMAND_SH);
                    os = new DataOutputStream(process.getOutputStream());
                    for (String command : commands) {
                        if (command != null) {
                            os.write(command.getBytes());
                            os.writeBytes(COMMAND_LINE_END);
                            os.flush();
                        }
                    }
                    os.writeBytes(COMMAND_EXIT);
                    os.flush();
                    commandResult.result = process.waitFor();
                    StringBuilder successMsg = new StringBuilder();
                    StringBuilder errorMsg = new StringBuilder();
                    successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while (true) {
                        String s = successResult.readLine();
                        if (s == null) {
                            break;
                        }
                        successMsg.append(s);
                    }
                    while (true) {
                        String s2 = errorResult.readLine();
                        if (s2 == null) {
                            break;
                        }
                        errorMsg.append(s2);
                    }
                    commandResult.successMsg = successMsg.toString();
                    commandResult.errorMsg = errorMsg.toString();
                    Log.i(TAG, commandResult.result + " | " + commandResult.successMsg + " | " + commandResult.errorMsg);
                    try {
                        os.close();
                        successResult.close();
                        errorResult.close();
                    } catch (IOException e) {
                        String errmsg = e.getMessage();
                        if (errmsg != null) {
                            Log.e(TAG, errmsg);
                        } else {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e2) {
                    String errmsg2 = e2.getMessage();
                    if (errmsg2 != null) {
                        Log.e(TAG, errmsg2);
                    } else {
                        e2.printStackTrace();
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e3) {
                            String errmsg3 = e3.getMessage();
                            if (errmsg3 != null) {
                                Log.e(TAG, errmsg3);
                            } else {
                                e3.printStackTrace();
                            }
                            if (process != null) {
                            }
                            return commandResult;
                        }
                    }
                    if (successResult != null) {
                        successResult.close();
                    }
                    if (errorResult != null) {
                        errorResult.close();
                    }
                    if (process != null) {
                    }
                }
            } catch (Exception e4) {
                String errmsg4 = e4.getMessage();
                if (errmsg4 != null) {
                    Log.e(TAG, errmsg4);
                } else {
                    e4.printStackTrace();
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e5) {
                        String errmsg5 = e5.getMessage();
                        if (errmsg5 != null) {
                            Log.e(TAG, errmsg5);
                        } else {
                            e5.printStackTrace();
                        }
                        if (process != null) {
                        }
                        return commandResult;
                    }
                }
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
                if (process != null) {
                }
            }
            if (process != null) {
                process.destroy();
            }
            return commandResult;
        } catch (Throwable th) {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e6) {
                    String errmsg6 = e6.getMessage();
                    if (errmsg6 != null) {
                        Log.e(TAG, errmsg6);
                    } else {
                        e6.printStackTrace();
                    }
                    if (process != null) {
                        process.destroy();
                    }
                    throw th;
                }
            }
            if (successResult != null) {
                successResult.close();
            }
            if (errorResult != null) {
                errorResult.close();
            }
            if (process != null) {
            }
            throw th;
        }
    }

    public static void runShell(String cmd, boolean needLog) throws IOException {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String content = in.readLine();
                if (content != null) {
                    stringBuffer.append(content);
                    if (needLog) {
                        Log.e(TAG, cmd + " !!!" + content);
                    }
                } else {
                    return;
                }
            }
        } catch (Exception e) {
        }
    }
}
