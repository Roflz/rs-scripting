public enum Scripts {
    FARMER_LVL("Farmer-lvl-based", "NA","local"),
    FARMER_GUI("Farmer-gui", "NA","local"),
    FIGHTER("Pro Fighter O","Pro Fighter", "SDN"),
    MINER("Pro Miner","Pro Miner", "SDN"),
    THIEVER("Pro Thiever","Pro Thiever", "SDN"),
    WOODCUTTER("Pro Woodcutter","Pro Woodcutter", "SDN");

    private final String scriptName;
    private final String scriptFolderName;
    private final String scriptType;

    Scripts(String scriptName, String scriptFolderName, String scriptType) {
        this.scriptName = scriptName;
        this.scriptFolderName = scriptFolderName;
        this.scriptType = scriptType;
    }

    public static Scripts getScriptFromName(String scriptName) throws Exception {
        for(Scripts script : Scripts.values()) {
            if(script.getScriptName() == scriptName) {
                return script;
            }
        }
        throw new Exception("Could not find script with name: " + scriptName);
    }

    public String getScriptName() {
        return scriptName;
    }

    public String getScriptType() {
        return scriptType;
    }

    public String getScriptFolderName() {
        return scriptFolderName;
    }
}
