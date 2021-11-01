import java.util.concurrent.ThreadLocalRandom;

public enum Worlds {
    WORLD_301("301",false,"trade"),
    WORLD_302("302",true,"trade"),
    WORLD_303("303",true,"-"),
    WORLD_304("304",true,"Trouble Brewing"),
    WORLD_305("305",true,"Falador Party Room"),
    WORLD_306("306",true,"Barbarian Assault"),
    WORLD_307("307",true,"Wintertodt"),
    WORLD_308("308",false,"Wilderness PK"),
    WORLD_309("309",true,"Wintertodt"),
    WORLD_310("310",true,"Barbarian Assault"),
    WORLD_311("311",true,"Wintertodt"),
    WORLD_312("312",true,"-"),
    WORLD_313("313",true,"-"),
    WORLD_314("314",true,"Brimhaven Agility Arena"),
    WORLD_315("315",true,"Fishing Trawler"),
    WORLD_316("316",false,"Wilderness PK"),
    WORLD_317("317",true,"-"),
    WORLD_318("318",true,"Target World"),
    WORLD_319("319",true,"Target World"),
    WORLD_320("320",true,"Soul Wars"),
    WORLD_321("321",true,"Sulliuscep cutting"),
    WORLD_322("322",true,"Clan Wars"),
    WORLD_323("323",true,"Volcanic Mine"),
    WORLD_324("324",true,"Group Iron"),
    WORLD_325("325",true,"Group Iron"),
    WORLD_326("326",false,"LMS Casual"),
    WORLD_327("327",true,"Ourania Altar"),
    WORLD_328("328",true,"Group Iron"),
    WORLD_329("329",true,"-"),
    WORLD_330("330",true,"House Party"),
    WORLD_331("331",true,"-"),
    WORLD_332("332",true,"-"),
    WORLD_333("333",true,"-"),
    WORLD_334("334",true,"Castle Wars"),
    WORLD_335("335",false,"Group Iron"),
    WORLD_336("336",true,"-"),
    WORLD_337("337",true,"Nightmare of Ashihama"),
    WORLD_338("338",true,"-"),
    WORLD_339("339",true,"-"),
    WORLD_340("340",true,"-"),
    WORLD_341("341",true,"Tempeross"),
    WORLD_342("342",true,"Role-playing"),
    WORLD_343("343",true,"-"),
    WORLD_344("344",true,"Pest control"),
    WORLD_345("345",true,"Deadman"),
    WORLD_346("346",true,"Agility Training"),
    WORLD_347("347",true,"-"),
    WORLD_348("348",true,"-"),
    WORLD_349("349",true,"2000 Skill total"),
    WORLD_350("350",true,"Soul Wars"),
    WORLD_351("351",true,"-"),
    WORLD_352("352",true,"Blast Furnace"),
    WORLD_353("353",true,"1250 Skill total"),
    WORLD_354("354",true,"Castle Wars"),
    WORLD_355("355",true,"Blast Furnace"),
    WORLD_356("356",true,"Blast Furnace"),
    WORLD_357("357",true,"Blast Furnace"),
    WORLD_358("358",true,"Blast Furnace"),
    WORLD_359("359",true,"-"),
    WORLD_360("360",true,"-"),
    WORLD_361("361",true,"2000 Skill total"),
    WORLD_362("362",true,"TzHaar Fight Pit"),
    WORLD_363("363",true,"2200 Skill total"),
    WORLD_364("364",true,"1250 Skill total"),
    WORLD_365("365",true,"High Risk"),
    WORLD_366("366",true,"1500 Skill total"),
    WORLD_367("367",true,"-"),
    WORLD_368("368",true,"-"),
    WORLD_369("369",true,"Wilderness PK"),
    WORLD_370("370",true,"Fishing Trawler"),
    WORLD_371("371",false,"Group Iron"),
    WORLD_372("372",false,"750 Skill total"),
    WORLD_373("373",true,"1750 Skill total"),
    WORLD_374("374",true,"Theatre of Blood"),
    WORLD_375("375",true,"Zalcano"),
    WORLD_376("376",true,"Theatre of Blood"),
    WORLD_377("377",true,"Mort'ton Temple"),
    WORLD_378("378",true,"Zalcano"),
    WORLD_379("379",false,"-"),
    WORLD_380("380",false,"-"),
    WORLD_381("381",false,"500 Skill total"),
    WORLD_382("382",false,"-"),
    WORLD_383("383",false,"Castle Wars"),
    WORLD_384("384",false,"-"),
    WORLD_386("386",true,"Blast Furnace"),
    WORLD_387("387",true,"Blast Furnace"),
    WORLD_388("388",true,"Theatre of Blood"),
    WORLD_389("389",true,"Wintertodt"),
    WORLD_390("390",true,"-"),
    WORLD_391("391",true,"1750 Skill total"),
    WORLD_392("392",true,"PvP"),
    WORLD_393("393",false,"750 Skill total"),
    WORLD_394("394",false,"Clan Wars"),
    WORLD_395("395",true,"Blast Furnace"),
    WORLD_396("396",true,"2000 Skill total"),
    WORLD_397("397",false,"-"),
    WORLD_398("398",false,"-"),
    WORLD_399("399",false,"-"),
    WORLD_413("413",false,"500 Skill total"),
    WORLD_414("414",false,"750 Skill total"),
    WORLD_415("415",true,"2200 Skill total"),
    WORLD_416("416",true,"1500 Skill total"),
    WORLD_417("417",false,"Group Iron"),
    WORLD_418("418",false,"-"),
    WORLD_419("419",false,"500 Skill total"),
    WORLD_420("420",true,"1500 Skill total"),
    WORLD_421("421",true,"-"),
    WORLD_422("422",true,"Tempeross"),
    WORLD_424("424",true,"Blast Furnace"),
    WORLD_425("425",false,"LMS Casual"),
    WORLD_426("426",false,"Group Iron"),
    WORLD_427("427",false,"500 Skill total"),
    WORLD_428("428",true,"2000 Skill total"),
    WORLD_429("429",true,"1250 Skill total"),
    WORLD_430("430",false,"-"),
    WORLD_431("431",false,"-"),
    WORLD_432("432",false,"750 Skill total"),
    WORLD_433("433",false,"-"),
    WORLD_434("434",false,"-"),
    WORLD_435("435",false,"-"),
    WORLD_436("436",false,"-"),
    WORLD_437("437",false,"-"),
    WORLD_443("443",true,"-"),
    WORLD_444("444",true,"-"),
    WORLD_445("445",true,"-"),
    WORLD_446("446",true,"Role-playing"),
    WORLD_447("447",true,"1250 Skill total"),
    WORLD_448("448",true,"1500 Skill total"),
    WORLD_449("449",true,"1750 Skill total"),
    WORLD_450("450",false,"2200 Skill total"),
    WORLD_451("451",false,"-"),
    WORLD_452("452",false,"-"),
    WORLD_453("453",false,"-"),
    WORLD_454("454",false,"-"),
    WORLD_455("455",false,"-"),
    WORLD_456("456",false,"-"),
    WORLD_463("463",true,"Tempeross"),
    WORLD_464("464",true,"-"),
    WORLD_465("465",true,"House Party"),
    WORLD_466("466",true,"Blast Furnace"),
    WORLD_467("467",true,"1750 Skill total"),
    WORLD_468("468",false,"500 Skill total"),
    WORLD_469("469",false,"LMS Casual"),
    WORLD_470("470",false,"-"),
    WORLD_471("471",false,"-"),
    WORLD_472("472",false,"-"),
    WORLD_473("473",false,"-"),
    WORLD_474("474",true,"High Risk"),
    WORLD_475("475",false,"-"),
    WORLD_476("476",false,"-"),
    WORLD_477("477",true,"Clan Recruitment"),
    WORLD_478("478",true,"-"),
    WORLD_479("479",true,"-"),
    WORLD_480("480",true,"Ourania Altar"),
    WORLD_481("481",true,"-"),
    WORLD_482("482",true,"-"),
    WORLD_483("483",false,"-"),
    WORLD_484("484",true,"-"),
    WORLD_485("485",true,"-"),
    WORLD_486("486",true,"-"),
    WORLD_487("487",true,"-"),
    WORLD_488("488",true,"-"),
    WORLD_489("489",true,"-"),
    WORLD_490("490",true,"-"),
    WORLD_491("491",true,"Burthorpe Games Room"),
    WORLD_492("492",true,"-"),
    WORLD_493("493",true,"Pyramid Plunder"),
    WORLD_494("494",true,"Blast Furnace");

    private final String worldNumber;
    private final boolean members;
    private final String worldType;

    Worlds(String worldNumber, boolean members, String worldType) {
        this.worldNumber = worldNumber;
        this.members = members;
        this.worldType = worldType;
    }

    public static Worlds getWorldFromNumber(String worldNumber) throws Exception {
        for(Worlds world : Worlds.values()) {
            if(worldNumber.contains(world.getWorldNumber())) {
                return world;
            }
        }
        throw new Exception("Could not find world " + worldNumber);
    }

    public static Worlds getRandomWorld(boolean members) throws Exception {
        int i = ThreadLocalRandom.current().nextInt(0, Worlds.values().length -10);
        int j = 0;
        for(Worlds world : Worlds.values()) {
            if(!world.worldType.contains("Skill total") && !world.worldType.contains("High Risk") && !world.worldType.contains("Deadman") && !world.worldType.contains("Group Iron") ) {
                if (members == world.isMembers()) {
                    if(i <= j) {
                        return world;
                    }
                }
            }
            j++;
        }
        throw new Exception("Could not pick random world");
    }

    public String getWorldNumber() {
        return worldNumber;
    }

    public boolean isMembers() {
        return members;
    }

    public String getWorldType() { return worldType; }
}
