import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public enum Accounts {
    DWEEB1234 ("dweeb1234@gmail.com", "retard3", "107.187.123.43", "1080", "Q2ODKAC7", "UFQOXU0P"),
    KISFORHORSES ("kisforhorses@gmail.com", "retard3", "107.187.123.244", "1080", "Q2ODKAC7", "UFQOXU0P"),
    FLEZROT ("flezrot123@gmail.com", "retard3", "107.165.181.158", "1080", "Q2ODKAC7", "UFQOXU0P"),
    FLEZ ("flez1234@gmail.com", "retard3", "107.165.181.158", "1080", "Q2ODKAC7", "UFQOXU0P"),
    CHODEZOR ("chodezor@gmail.com", "retard3", "104.164.151.188", "1080", "Q2ODKAC7", "UFQOXU0P"),
    NOOBZAR ("iamanoobzar@gmail.com", "retard3", "107.164.51.214", "1080", "Q2ODKAC7", "UFQOXU0P"),
    ISUCK ("isuck1234@gmail.com", "retard3", "107.165.181.130", "1080", "Q2ODKAC7", "UFQOXU0P"),
    FOURTWENTY ("fourtwenty420@gmail.com", "retard3", "107.187.39.68", "1080", "Q2ODKAC7", "UFQOXU0P"),
    ONLYFANS ("onlymyfans@gmail.com", "retard3", "107.165.181.167", "1080", "Q2ODKAC7", "UFQOXU0P"),
    GASTRO ("Gastrofecality@gmail.com", "retard3", "107.187.42.55", "1080", "Q2ODKAC7", "UFQOXU0P"),
    DUMBASS("dumbass@gfunk.gov","retard3","104.164.151.188","1080","Q2ODKAC7","UFQOXU0P"),
    GUCCI("guccimane@gfunk.gov","retard3","107.187.123.43","1080","Q2ODKAC7","UFQOXU0P"),
    FUNKSAUCE("funksauce@gfunk.gov","retard3","107.164.51.214","1080","Q2ODKAC7","UFQOXU0P"),
    SEXYBOSS("sexyboss@love.sex","retard3","104.164.151.188","1080","Q2ODKAC7","UFQOXU0P"),
    BLOWJOBS("blowjobs@love.sex","retard3","107.187.123.43","1080","Q2ODKAC7","UFQOXU0P"),
    SEXYTIMES("sexytimes@love.sex","retard3","107.164.51.214","1080","Q2ODKAC7","UFQOXU0P"),
    NIPPLETWIST("nippletwist@nips.tit","retard3","107.165.181.130","1080","Q2ODKAC7","UFQOXU0P"),
    BIGBOOBS("bigboobs@nips.tit","retard3","107.165.181.130","1080","Q2ODKAC7","UFQOXU0P"),
    HARDNIPS("hardnipples@nips.tit","retard3","107.187.42.55","1080","Q2ODKAC7","UFQOXU0P"),
    FIRENUGS("firenugs@weed.tree","retard3","107.164.51.107","1080","Q2ODKAC7","UFQOXU0P"),
    SOFTNIPS("softnipples@nips.tit","retard3","107.187.42.55","1080","Q2ODKAC7","UFQOXU0P"),
    SMOKEMEOWT("smokemeowt@weed.tree","retard3","107.164.51.107","1080","Q2ODKAC7","UFQOXU0P"),
    DANKBUDS("dankbuds@weed.tree","retard3","107.164.51.107","1080","Q2ODKAC7","UFQOXU0P");

    private final String accountName;
    private final String accountPassword;
    private final String ip;
    private final String port;
    private final String ipUsername;
    private final String ipPassword;

    Accounts(String accountName, String accountPassword, String ip, String port, String ipUsername, String ipPassword) {
        this.accountName = accountName;
        this.accountPassword = accountPassword;
        this.ip = ip;
        this.port = port;
        this.ipUsername = ipUsername;
        this.ipPassword = ipPassword;
    }

    public static Accounts getAccountFromName(Object accountName) throws Exception {
        for(Accounts account : Accounts.values()) {
            if(account.getAccountName() == accountName) {
                return account;
            }
        }
        throw new Exception("Could not find account with name: " + accountName);
    }

    public static List<String> getAccountNames() {
        List<String> nameList = new ArrayList();
        for(Accounts account: Accounts.values()) {
            nameList.add(account.getAccountName());
        }
        return nameList;
    }

    public static DefaultListModel addNamesToModel(DefaultListModel model) {
        for(Accounts account: Accounts.values()) {
            model.addElement(account.getAccountName());
        }
        return model;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountPassword() {
        return accountPassword;
    }

    public String getIp() {
        return ip;
    }

    public String getIpPassword() {
        return ipPassword;
    }

    public String getIpUsername() {
        return ipUsername;
    }

    public String getPort() {
        return port;
    }
}
