package com.roflz.bots.contructor;

import com.epicbot.api.shared.script.LoopScript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Construction extends LoopScript {

    public static String watDo = "";

    private List<String> startingItemsList = Arrays.asList("Pestle and mortar", "Vial", "Tinderbox", "Cup of tea", "Rope", "Opal", "Charoal", "Varrock teleport");
    private List<Integer> startingQuantitiesList = Arrays.asList(1, 1, 1, 1, 2, 1, 1, 2);
    public static List<String> shoppingListItems = new ArrayList();
    public static List<Integer> shoppingListQuantities = new ArrayList();
    private int geBuyCounter = 0;
    private int gePriceIncreaseCounter = 1;

    @Override
    protected int loop() {
        switch (watDo) {
            case "":
                getAPIContext().camera().setPitch(98);
                getAPIContext().mouse().move(getAPIContext().game().getCenterSceneTile().getCentralPoint());
                getAPIContext().mouse().scroll(false, 20);
                watDo = "Bank";
                return 500;

        return 1000;
    }

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    public int getItemQuantity(String item) {
        int i = 0;
        for (String startingItem : startingItemsList) {
            if (startingItem.contains(item)) {
                return startingQuantitiesList.get(i);
            }
            i += 1;
        }
        return 0;
    }
}
