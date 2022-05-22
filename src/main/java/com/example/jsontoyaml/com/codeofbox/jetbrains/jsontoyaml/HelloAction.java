package com.example.jsontoyaml.com.codeofbox.jetbrains.jsontoyaml;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.history.core.Paths;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class HelloAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        PsiFile psiFile = event.getData(LangDataKeys.PSI_FILE);
        if (!psiFile.getVirtualFile().getName().endsWith(".yml") && !psiFile.getVirtualFile().getName().endsWith(".yaml")) {
            return;
        }
        String canonicalPath = psiFile.getVirtualFile().getCanonicalPath();
        String newFileName = null;
        if (canonicalPath.endsWith(".yml")) {
            newFileName = canonicalPath.substring(0, canonicalPath.length() - 4) + ".json";
        } else {
            newFileName = canonicalPath.substring(0, canonicalPath.length() - 5) + ".json";
        }
        showPopupBalloon(editor, "generate " + newFileName);

        File file = new File(newFileName);
        PropertyUtils propUtils = new PropertyUtils();

        propUtils.setSkipMissingProperties(true);

        Representer repr = new Representer();

        repr.setPropertyUtils(propUtils);

        Yaml yaml = new Yaml(new Constructor(), repr);

        try {
            byte[] bytes = psiFile.getVirtualFile().contentsToByteArray();
            String yamlStr = new String(bytes, StandardCharsets.UTF_8);
            Map load = yaml.loadAs(yamlStr, Map.class);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonStr = gson.toJson(load);
            Files.write(file.toPath(), jsonStr.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            showPopupBalloon(editor, "generate " + newFileName + " fail, messge:" + e.getMessage());
        }

    }


    private void showPopupBalloon(final Editor editor, final String result) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
                JBPopupFactory factory = JBPopupFactory.getInstance();
                factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                        .setFadeoutTime(5000)
                        .createBalloon()
                        .show(factory.guessBestPopupLocation(editor), Balloon.Position.below);
            }
        });
    }
}
