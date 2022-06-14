package org.aya.intellij.settings;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.openapi.options.colors.RainbowColorSettingsPage;
import com.intellij.openapi.util.NlsContexts;
import org.aya.intellij.AyaIcons;
import org.aya.intellij.AyaLanguage;
import org.aya.intellij.actions.SyntaxHighlight;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class AyaColorSettingsPage implements ColorSettingsPage, RainbowColorSettingsPage {
  private static final AttributesDescriptor[] DESCRIPTORS = new AttributesDescriptor[]{
    new AttributesDescriptor("Keyword", SyntaxHighlight.KEYWORD),
    new AttributesDescriptor("Identifier", SyntaxHighlight.ID),
    new AttributesDescriptor("String literal", SyntaxHighlight.STRING),
    new AttributesDescriptor("Number literal", SyntaxHighlight.NUMBER),
    new AttributesDescriptor("Function definition", SyntaxHighlight.FN_DEF),
    new AttributesDescriptor("Function call", SyntaxHighlight.FN_CALL),
    new AttributesDescriptor("Primitive definition", SyntaxHighlight.PRIM_DEF),
    new AttributesDescriptor("Primitive call", SyntaxHighlight.PRIM_CALL),
    new AttributesDescriptor("Data definition", SyntaxHighlight.DATA_DEF),
    new AttributesDescriptor("Data call", SyntaxHighlight.DATA_CALL),
    new AttributesDescriptor("Constructor definition", SyntaxHighlight.CON_DEF),
    new AttributesDescriptor("Constructor call", SyntaxHighlight.CON_CALL),
    new AttributesDescriptor("Struct definition", SyntaxHighlight.STRUCT_DEF),
    new AttributesDescriptor("Struct call", SyntaxHighlight.STRUCT_CALL),
    new AttributesDescriptor("Field definition", SyntaxHighlight.FIELD_DEF),
    new AttributesDescriptor("Field call", SyntaxHighlight.FIELD_CALL),
  };

  @Override public @Nullable Icon getIcon() {
    return AyaIcons.AYA_FILE;
  }

  @Override public @NotNull SyntaxHighlighter getHighlighter() {
    return new SyntaxHighlight();
  }

  @Override public @NonNls @NotNull String getDemoText() {
    return """
      open data Nat | zero | suc Nat
      """.stripIndent();
  }

  @Override public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
    return null;
  }

  @Override public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
    return DESCRIPTORS;
  }

  @Override public ColorDescriptor @NotNull [] getColorDescriptors() {
    return ColorDescriptor.EMPTY_ARRAY;
  }

  @Override public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
    return AyaLanguage.INSTANCE.getDisplayName();
  }

  @Override public boolean isRainbowType(TextAttributesKey type) {
    return SyntaxHighlight.LSP.equals(type);
  }

  @Override public @Nullable Language getLanguage() {
    return AyaLanguage.INSTANCE;
  }
}
