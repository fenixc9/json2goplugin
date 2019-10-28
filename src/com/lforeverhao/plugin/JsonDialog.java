package com.lforeverhao.plugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.lforeverhao.plugin.json.JSONArray;
import com.lforeverhao.plugin.json.JSONException;
import com.lforeverhao.plugin.json.JSONObject;
import com.lforeverhao.plugin.model.go.Struct;
import com.lforeverhao.plugin.model.json.Parser;
import org.apache.http.util.TextUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class JsonDialog extends JFrame {

    private final Editor editor;
    private CardLayout cardLayout;
    private JPanel contentPane2;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel errorLB;
    private JTextPane editTP;

    private JButton settingButton;
    private JLabel generateClassLB;
    private JTextField generateClassTF;
    private JPanel generateClassP;
    private JButton formatBtn;
    private JTextPane rootTypeName;
    private PsiFile file;
    private Project project;
    private String errorInfo = null;

    public JsonDialog(Editor editor, PsiFile file, Project project) throws HeadlessException {
        this.editor = editor;
        this.file = file;
        this.project = project;
        setContentPane(contentPane2);
        setTitle("Json2go");
        getRootPane().setDefaultButton(okButton);
        this.setAlwaysOnTop(true);
        initGeneratePanel(file);
        initListener();
    }

    private void initListener() {

        okButton.addActionListener(e -> {
            if (generateClassTF.isFocusOwner()) {
                editTP.requestFocus(true);
            } else {
                onOK_();
            }
        });
        formatBtn.addActionListener(e -> {
            try {
                String json = editTP.getText();
                json = json.trim();
                if (json.startsWith("{")) {
                    JSONObject jsonObject = new JSONObject(json);
                    String formatJson = jsonObject.toString(4);
                    editTP.setText(formatJson);
                } else if (json.startsWith("[")) {
                    JSONArray jsonArray = new JSONArray(json);
                    String formatJson = jsonArray.toString(4);
                    editTP.setText(formatJson);
                }
            } catch (JSONException ex) {
                errorLB.setText("转换json失败");
            }

        });
        editTP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    onOK_();
                }
            }
        });
        generateClassP.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                super.keyReleased(keyEvent);
                if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER) {
                    editTP.requestFocus(true);
                }
            }
        });
        errorLB.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                if (errorInfo != null) {
                    ErrorDialog errorDialog = new ErrorDialog(errorInfo);
                    errorDialog.setSize(800, 600);
                    errorDialog.setLocationRelativeTo(null);
                    errorDialog.setVisible(true);
                }
            }
        });
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
        settingButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openSettingDialog();
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        contentPane2.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void initGeneratePanel(PsiFile file) {

        cardLayout = (CardLayout) generateClassP.getLayout();
        generateClassTF.setBackground(errorLB.getBackground());
        generateClassTF.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
            }

            @Override
            public void focusLost(FocusEvent focusEvent) {
                cardLayout.next(generateClassP);
                if (TextUtils.isEmpty(generateClassTF.getText())) {
                } else {
                    generateClassLB.setText(generateClassTF.getText());
                }
            }
        });
    }

    private void onOK_() {
        String jsonSTR = editTP.getText().trim();
        if (TextUtils.isEmpty(jsonSTR)) {
            errorLB.setText("请输入json");
            return;
        }
        String rootType = rootTypeName.getText().trim();
        if (TextUtils.isEmpty(jsonSTR) || rootType.contains("请输入")) {
            errorLB.setText("请输入root type");
            return;
        }
        Parser root = new Parser(jsonSTR, rootType);
        List<Struct> structs = root.parseStruct();
        if (structs == null || structs.size() == 0) {
            errorLB.setText("转换json失败！");
            return;
        }
        String path = file.getVirtualFile().getPath();
        try {
            writeData(structs, path);
            file.getVirtualFile().refresh(false, false);
        } catch (IOException e) {
            errorLB.setText("写入失败:" + e.getLocalizedMessage());
            return;
        }
        dispose();
    }

    private void writeData(List<Struct> structs, String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Struct struct : structs) {
            sb.append(struct.toGolangCode());
            sb.append("\n");
        }
        Files.write(Paths.get(path), sb.toString().getBytes(), StandardOpenOption.APPEND);
    }

    private void onCancel() {
        dispose();
    }

    public void setProject(Project mProject) {
        this.project = mProject;
    }

    public void setFile(PsiFile mFile) {
        this.file = mFile;
    }

    private void createUIComponents() {

    }

    public void openSettingDialog() {
        errorLB.setText("尚未实现");
    }
}
