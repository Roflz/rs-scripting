import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGUI {
    private static JFrame frame = new JFrame();
    private JPanel rootPanel;
    private JRadioButton allotmentButton;
    private JRadioButton flowerButton;
    private JRadioButton herbButton;
    private JRadioButton hopsButton;
    private JComboBox allotmentComboBox;
    private JComboBox flowerComboBox;
    private JComboBox herbComboBox;
    private JComboBox hopsComboBox;
    private JComboBox compostBox;

    public ClientGUI() {
        allotmentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allotmentButton.isSelected()) {
                    FarmingUtils.farmAllotments = true;
                    allotmentComboBox.setEnabled(true);
                    FarmingUtils.allotmentCrop = Crops.getCropfromName((String)allotmentComboBox.getSelectedItem());
                } else if(!allotmentButton.isSelected()) {
                    FarmingUtils.farmAllotments = false;
                    allotmentComboBox.setEnabled(false);
                    FarmingUtils.allotmentCrop = null;
                }
            }
        });
        flowerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(flowerButton.isSelected()) {
                    FarmingUtils.farmFlowers = true;
                    flowerComboBox.setEnabled(true);
                    FarmingUtils.flowerCrop = Crops.getCropfromName((String)flowerComboBox.getSelectedItem());
                } else if(!flowerButton.isSelected()) {
                    FarmingUtils.farmFlowers = false;
                    flowerComboBox.setEnabled(false);
                    FarmingUtils.flowerCrop = null;
                }
            }
        });
        herbButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(herbButton.isSelected()) {
                    FarmingUtils.farmHerbs = true;
                    herbComboBox.setEnabled(true);
                    FarmingUtils.herbCrop = Crops.getCropfromName((String)herbComboBox.getSelectedItem());
                } else if(!herbButton.isSelected()) {
                    FarmingUtils.farmHerbs = false;
                    herbComboBox.setEnabled(false);
                    FarmingUtils.herbCrop = null;
                }
            }
        });
        hopsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(hopsButton.isSelected()) {
                    FarmingUtils.farmHops = true;
                    hopsComboBox.setEnabled(true);
                    FarmingUtils.hopsCrop = Crops.getCropfromName((String)hopsComboBox.getSelectedItem());
                } else if(!hopsButton.isSelected()) {
                    FarmingUtils.farmHops = false;
                    hopsComboBox.setEnabled(false);
                    FarmingUtils.hopsCrop = null;
                }
            }
        });
        allotmentComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allotmentComboBox.isEnabled()) {
                    FarmingUtils.allotmentCrop = Crops.getCropfromName((String)allotmentComboBox.getSelectedItem());
                }
            }
        });
        flowerComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(flowerComboBox.isEnabled()) {
                    FarmingUtils.flowerCrop = Crops.getCropfromName((String)flowerComboBox.getSelectedItem());
                }
            }
        });
        herbComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(herbComboBox.isEnabled()) {
                    FarmingUtils.herbCrop = Crops.getCropfromName((String)herbComboBox.getSelectedItem());
                }
            }
        });
        hopsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(hopsComboBox.isEnabled()) {
                    FarmingUtils.hopsCrop = Crops.getCropfromName((String)hopsComboBox.getSelectedItem());
                }
            }
        });
        compostBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FarmingUtils.compostType = StoreItems.getItemfromName((String)compostBox.getSelectedItem());
            }
        });
    }

    public boolean isOpen() {
        return frame.isVisible();
    }

    private void setName(String name) {
        frame.setName(main.getPlayerName());
    }

    public static void main(String[] args) {
        frame.setContentPane(new ClientGUI().rootPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void initUI() {

        FarmingUtils.compostType = StoreItems.getItemfromName((String)compostBox.getSelectedItem());
        FarmingUtils.allotmentCrop = Crops.getCropfromName((String)allotmentComboBox.getSelectedItem());
        FarmingUtils.flowerCrop = Crops.getCropfromName((String)flowerComboBox.getSelectedItem());
        FarmingUtils.herbCrop = Crops.getCropfromName((String)herbComboBox.getSelectedItem());
        FarmingUtils.hopsCrop = Crops.getCropfromName((String)hopsComboBox.getSelectedItem());
        if(allotmentButton.isSelected()) { FarmingUtils.farmAllotments = true; }
        if(flowerButton.isSelected()) { FarmingUtils.farmFlowers = true; }
        if(herbButton.isSelected()) { FarmingUtils.farmHerbs = true; }
        if(hopsButton.isSelected()) { FarmingUtils.farmHops = true; }

        setName(main.getPlayerName());
        frame.setContentPane(new ClientGUI().rootPanel);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
