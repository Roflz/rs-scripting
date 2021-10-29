import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class BotLaunch {
    private static JFrame frame = new JFrame();
    private JPanel rootPanel;
    private JRadioButton randomWorld;
    private JComboBox script;
    private JComboBox worlds;
    private JScrollPane scrollPane;
    private JButton startButton;
    public static DefaultListModel model = new DefaultListModel();
    public static DefaultListModel runModel = new DefaultListModel();
    public static DefaultComboBoxModel scriptProfileModel = new DefaultComboBoxModel();
    private JList accountRunList;
    private JButton addButton;
    private JList accountList;
    private JButton removeButton;
    public JCheckBox members;
    public JComboBox scriptProfile;
    private JComboBox schedule;
    private String scriptProfilePath = "C:\\Users\\mahnr\\EpicBot\\Script Settings\\";

    public void initAccountListModel() {
        for (String name : Accounts.getAccountNames()) {
            model.addElement(name);
        }
    }

    public void initScriptProfileModel(String scriptFolderName) {
        File folder;
        if((String) script.getSelectedItem() == "Pro Fighter O") {
            folder = new File(scriptProfilePath + scriptFolderName);
        } else {
            folder = new File(scriptProfilePath + scriptFolderName + "\\profiles\\");
        }
        File[] listOfFiles = folder.listFiles();
        if(scriptFolderName != "NA") {
            scriptProfile.setEnabled(true);
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    scriptProfileModel.addElement(listOfFiles[i].getName());
                }
            }
            System.out.println(scriptProfileModel);
        } else {
            scriptProfileModel.removeAllElements();
            scriptProfile.setEnabled(false);
        }

    }

    public BotLaunch() {
        randomWorld.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(randomWorld.isSelected()) {
                    worlds.setEnabled(false);
                    members.setEnabled(true);
                    CmdTest.randomWorld = true;
                    try {
                        if(members.isSelected()) {
                            CmdTest.world = Worlds.getRandomWorld(true);
                        } else {
                            CmdTest.world = Worlds.getRandomWorld(false);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else if(!randomWorld.isSelected()) {
                    CmdTest.randomWorld = false;
                    worlds.setEnabled(true);
                    members.setEnabled(false);
                }
            }
        });
        script.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CmdTest.script = Scripts.getScriptFromName((String) script.getSelectedItem());
                    initScriptProfileModel(Scripts.getScriptFromName((String) script.getSelectedItem()).getScriptFolderName());
                    scriptProfile.removeAllItems();
                    scriptProfile.setModel((ComboBoxModel) scriptProfileModel);
                    CmdTest.scriptProfile = scriptProfilePath + CmdTest.script.getScriptFolderName() + "\\" + (String) scriptProfile.getSelectedItem();
                    if(scriptProfileModel.getSize() == 0) {
                        scriptProfile.setEnabled(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        worlds.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    CmdTest.world = Worlds.getWorldFromNumber((String) worlds.getSelectedItem());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(false);
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Object name : accountList.getSelectedValuesList()) {
                    model.removeElement(name);
                    runModel.addElement(name);
                }
                accountList.setModel(model);
                accountRunList.setModel(runModel);

            }
        });
        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Object name : accountRunList.getSelectedValuesList()) {
                    runModel.removeElement(name);
                    model.addElement(name);
                }
                accountList.setModel(model);
                accountRunList.setModel(runModel);
            }
        });
        members.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(members.isSelected()) {
                    CmdTest.members = true;
                    try {
                        if(randomWorld.isSelected()) {
                            CmdTest.world = Worlds.getRandomWorld(true);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    CmdTest.members = false;
                    try {
                        if(randomWorld.isSelected()) {
                            CmdTest.world = Worlds.getRandomWorld(false);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        scriptProfile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CmdTest.scriptProfile = scriptProfilePath + CmdTest.script.getScriptFolderName() + "\\" + (String) scriptProfile.getSelectedItem();
            }
        });
        schedule.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CmdTest.schedule = (String) schedule.getSelectedItem();
                System.out.println(CmdTest.schedule);
            }
        });
    }

    public boolean isOpen() {
        return frame.isVisible();
    }

    public void initUI() {
        try {
            CmdTest.script = Scripts.getScriptFromName((String) script.getSelectedItem());
            CmdTest.schedule = (String) schedule.getSelectedItem();
            CmdTest.world = Worlds.getRandomWorld(true);
            if(randomWorld.isSelected()) { worlds.setEnabled(false); ; }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        initAccountListModel();
        System.out.println(CmdTest.script.getScriptFolderName());
        System.out.println(CmdTest.schedule);
        initScriptProfileModel(CmdTest.script.getScriptFolderName());

        frame.setContentPane(new BotLaunch().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

}
