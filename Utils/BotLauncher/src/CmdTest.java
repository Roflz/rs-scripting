import javax.swing.*;
import java.io.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdTest {
    public static Accounts account;
    public static Scripts script;
    public static Worlds world;
    public static String scriptProfile;
    public static String schedule;
    public static boolean randomWorld = true;
    public static boolean members = true;
    public static List<Process> processList = new ArrayList();

    public static void main(String[] args) throws Exception {
        BotLaunch gui = new BotLaunch();

        gui.initUI();

        while(gui.isOpen()) {
            if(!gui.isOpen()) { System.out.println("Starting Bot"); break; }
            Thread.sleep(1000);
        }
        for(Object thisAccount : BotLaunch.runModel.toArray()) {
            account = Accounts.getAccountFromName(thisAccount);
            if(randomWorld == true) {
                world = Worlds.getRandomWorld(members);
            }
            System.out.println("Running " +script.getScriptName()+ " with account: " +account+ " in world " +world.getWorldNumber());
            if(schedule != "None") {
                System.out.println("java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -schedule=\"" + schedule + "\" -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());

                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -schedule=\"" + schedule + "\" -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line.contains("Applet started")) { break; }
                    System.out.println(line);
                }
            } else if(scriptProfile == null) {
                System.out.println("java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -script=\"" + script.getScriptName() + "\" -scripttype=" + script.getScriptType() + " -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());

                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -script=\"" + script.getScriptName() + "\" -scripttype=" + script.getScriptType() + " -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());
                builder.redirectErrorStream(true);
                Process p = builder.start();
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line.contains("Applet started")) { break; }
                    System.out.println(line);
                }
            } else {
                System.out.println("java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -script=\"" + script.getScriptName() + "\" -scripttype=" + script.getScriptType() + " -scriptprofile=\"" +scriptProfile+ "\" -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());
                ProcessBuilder builder = new ProcessBuilder(
                        "cmd.exe", "/c", "java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -script=\"" + script.getScriptName() + "\" -scripttype=" + script.getScriptType() + " -scriptprofile=\"" +scriptProfile+ "\" -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());
                builder.redirectErrorStream(true);
                Process p = builder.start();

                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while (true) {
                    line = r.readLine();
                    if (line.contains("Starting random: Login")) { break; }
                    System.out.println(line);
                }
            }
        }
//        System.out.println("Running " +script.getScriptName()+ " with account: " +account.getAccountName()+ " in world " +world.getWorldNumber());
//
//        ProcessBuilder builder = new ProcessBuilder(
//                "cmd.exe", "/c", "java -cp \"/Users/mahnr/EpicBot/Dependencies/*\" com.epicbot.Boot -cli=true" + " -world=" + world.getWorldNumber() + " -rsusername=" + account.getAccountName() + " -rspassword=" + account.getAccountPassword() + " -script=" + script.getScriptName() + " -scripttype=" + script.getScriptType() + " -proxyhost=" + account.getIp() + " -proxyport=" + account.getPort() + " -proxyusername=" + account.getIpUsername() + " -proxypassword=" + account.getIpPassword());
//        builder.redirectErrorStream(true);
//        Process p = builder.start();
//        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
//        String line;
//        while (true) {
//            line = r.readLine();
//            if (line == null) { break; }
//            System.out.println(line);
//        }
    }
}