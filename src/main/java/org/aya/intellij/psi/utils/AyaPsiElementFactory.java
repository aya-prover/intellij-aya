package org.aya.intellij.psi.utils;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.aya.intellij.psi.concrete.impl.*;

import static org.aya.parser.AyaPsiElementTypes.*;

public class AyaPsiElementFactory {
  public static PsiElement createElement(ASTNode node) {
    IElementType type = node.getElementType();
    if (type == APP_EXPR) {
      return new AyaPsiAppExprImpl(node);
    } else if (type == ARRAY_COMP_BLOCK) {
      return new AyaPsiArrayCompBlockImpl(node);
    } else if (type == ARRAY_ELEMENTS_BLOCK) {
      return new AyaPsiArrayElementsBlockImpl(node);
    } else if (type == ARRAY_EXPR) {
      return new AyaPsiArrayExprImpl(node);
    } else if (type == ARROW_EXPR) {
      return new AyaPsiArrowExprImpl(node);
    } else if (type == ASSOC) {
      return new AyaPsiAssocImpl(node);
    } else if (type == ATOM_ABSURD_PATTERN) {
      return new AyaPsiAtomAbsurdPatternImpl(node);
    } else if (type == ATOM_BIND_PATTERN) {
      return new AyaPsiAtomBindPatternImpl(node);
    } else if (type == ATOM_CALM_FACE_PATTERN) {
      return new AyaPsiAtomCalmFacePatternImpl(node);
    } else if (type == ATOM_EX_ARGUMENT) {
      return new AyaPsiAtomExArgumentImpl(node);
    } else if (type == ATOM_EX_PATTERN) {
      return new AyaPsiAtomExPatternImpl(node);
    } else if (type == ATOM_IM_PATTERN) {
      return new AyaPsiAtomImPatternImpl(node);
    } else if (type == ATOM_NUMBER_PATTERN) {
      return new AyaPsiAtomNumberPatternImpl(node);
    } else if (type == ATOM_PATTERNS) {
      return new AyaPsiAtomPatternsImpl(node);
    } else if (type == ATOM_TUPLE_EXPR) {
      return new AyaPsiAtomTupleExprImpl(node);
    } else if (type == ATOM_ULIFT_EXPR) {
      return new AyaPsiAtomUliftExprImpl(node);
    } else if (type == BARE_CLAUSE) {
      return new AyaPsiBareClauseImpl(node);
    } else if (type == BARE_SUB_SYSTEM) {
      return new AyaPsiBareSubSystemImpl(node);
    } else if (type == BARRED) {
      return new AyaPsiBarredImpl(node);
    } else if (type == BARRED_CLAUSE) {
      return new AyaPsiBarredClauseImpl(node);
    } else if (type == BARRED_SUB_SYSTEM) {
      return new AyaPsiBarredSubSystemImpl(node);
    } else if (type == BIND_BLOCK) {
      return new AyaPsiBindBlockImpl(node);
    } else if (type == CALM_FACE_EXPR) {
      return new AyaPsiCalmFaceExprImpl(node);
    } else if (type == CLAUSE) {
      return new AyaPsiClauseImpl(node);
    } else if (type == CLAUSES) {
      return new AyaPsiClausesImpl(node);
    } else if (type == DATA_BODY) {
      return new AyaPsiDataBodyImpl(node);
    } else if (type == DATA_CTOR) {
      return new AyaPsiDataCtorImpl(node);
    } else if (type == DATA_CTOR_CLAUSE) {
      return new AyaPsiDataCtorClauseImpl(node);
    } else if (type == DATA_DECL) {
      return new AyaPsiDataDeclImpl(node);
    } else if (type == DECL) {
      return new AyaPsiDeclImpl(node);
    } else if (type == DECL_NAME_OR_INFIX) {
      return new AyaPsiDeclNameOrInfixImpl(node);
    } else if (type == DO_BINDING) {
      return new AyaPsiDoBindingImpl(node);
    } else if (type == DO_BLOCK) {
      return new AyaPsiDoBlockImpl(node);
    } else if (type == DO_BLOCK_CONTENT) {
      return new AyaPsiDoBlockContentImpl(node);
    } else if (type == DO_EXPR) {
      return new AyaPsiDoExprImpl(node);
    } else if (type == EXPR_LIST) {
      return new AyaPsiExprListImpl(node);
    } else if (type == FN_BODY) {
      return new AyaPsiFnBodyImpl(node);
    } else if (type == FN_DECL) {
      return new AyaPsiFnDeclImpl(node);
    } else if (type == FN_MODIFIERS) {
      return new AyaPsiFnModifiersImpl(node);
    } else if (type == FORALL_EXPR) {
      return new AyaPsiForallExprImpl(node);
    } else if (type == GENERALIZE) {
      return new AyaPsiGeneralizeImpl(node);
    } else if (type == GENERALIZE_PARAM_NAME) {
      return new AyaPsiGeneralizeParamNameImpl(node);
    } else if (type == GOAL_EXPR) {
      return new AyaPsiGoalExprImpl(node);
    } else if (type == HIDE_LIST) {
      return new AyaPsiHideListImpl(node);
    } else if (type == IDIOM_BLOCK) {
      return new AyaPsiIdiomBlockImpl(node);
    } else if (type == IDIOM_EXPR) {
      return new AyaPsiIdiomExprImpl(node);
    } else if (type == IDS_COMMA) {
      return new AyaPsiIdsCommaImpl(node);
    } else if (type == IMPORT_CMD) {
      return new AyaPsiImportCmdImpl(node);
    } else if (type == LAMBDA_EXPR) {
      return new AyaPsiLambdaExprImpl(node);
    } else if (type == LAMBDA_TELE) {
      return new AyaPsiLambdaTeleImpl(node);
    } else if (type == LAMBDA_TELE_BINDER) {
      return new AyaPsiLambdaTeleBinderImpl(node);
    } else if (type == LAMBDA_TELE_EX) {
      return new AyaPsiLambdaTeleExImpl(node);
    } else if (type == LAMBDA_TELE_IM) {
      return new AyaPsiLambdaTeleImImpl(node);
    } else if (type == LAMBDA_TELE_LIT) {
      return new AyaPsiLambdaTeleLitImpl(node);
    } else if (type == LIST_COMP) {
      return new AyaPsiListCompImpl(node);
    } else if (type == LIT_INT_EXPR) {
      return new AyaPsiLitIntExprImpl(node);
    } else if (type == LIT_STRING_EXPR) {
      return new AyaPsiLitStringExprImpl(node);
    } else if (type == LOOSERS) {
      return new AyaPsiLoosersImpl(node);
    } else if (type == MATCH_EXPR) {
      return new AyaPsiMatchExprImpl(node);
    } else if (type == MODULE) {
      return new AyaPsiModuleImpl(node);
    } else if (type == NAMED_IM_ARGUMENT) {
      return new AyaPsiNamedImArgumentImpl(node);
    } else if (type == NEW_ARG) {
      return new AyaPsiNewArgImpl(node);
    } else if (type == NEW_ARG_FIELD) {
      return new AyaPsiNewArgFieldImpl(node);
    } else if (type == NEW_BODY) {
      return new AyaPsiNewBodyImpl(node);
    } else if (type == NEW_EXPR) {
      return new AyaPsiNewExprImpl(node);
    } else if (type == OPEN_CMD) {
      return new AyaPsiOpenCmdImpl(node);
    } else if (type == OPEN_KW) {
      return new AyaPsiOpenKwImpl(node);
    } else if (type == PARTIAL_EXPR) {
      return new AyaPsiPartialExprImpl(node);
    } else if (type == PATH_EXPR) {
      return new AyaPsiPathExprImpl(node);
    } else if (type == PATH_TELE) {
      return new AyaPsiPathTeleImpl(node);
    } else if (type == PATTERN) {
      return new AyaPsiPatternImpl(node);
    } else if (type == PATTERNS) {
      return new AyaPsiPatternsImpl(node);
    } else if (type == PI_EXPR) {
      return new AyaPsiPiExprImpl(node);
    } else if (type == PRIM_DECL) {
      return new AyaPsiPrimDeclImpl(node);
    } else if (type == PRIM_NAME) {
      return new AyaPsiPrimNameImpl(node);
    } else if (type == PROJ_EXPR) {
      return new AyaPsiProjExprImpl(node);
    } else if (type == PROJ_FIX) {
      return new AyaPsiProjFixImpl(node);
    } else if (type == PROJ_FIX_ID) {
      return new AyaPsiProjFixIdImpl(node);
    } else if (type == QUALIFIED_ID) {
      return new AyaPsiQualifiedIdImpl(node);
    } else if (type == REF_EXPR) {
      return new AyaPsiRefExprImpl(node);
    } else if (type == REMARK) {
      return new AyaPsiRemarkImpl(node);
    } else if (type == SAMPLE_MODIFIERS) {
      return new AyaPsiSampleModifiersImpl(node);
    } else if (type == SIGMA_EXPR) {
      return new AyaPsiSigmaExprImpl(node);
    } else if (type == STRUCT_DECL) {
      return new AyaPsiStructDeclImpl(node);
    } else if (type == STRUCT_FIELD) {
      return new AyaPsiStructFieldImpl(node);
    } else if (type == SUB_SYSTEM) {
      return new AyaPsiSubSystemImpl(node);
    } else if (type == TELE) {
      return new AyaPsiTeleImpl(node);
    } else if (type == TELE_BINDER) {
      return new AyaPsiTeleBinderImpl(node);
    } else if (type == TELE_BINDER_ANONYMOUS) {
      return new AyaPsiTeleBinderAnonymousImpl(node);
    } else if (type == TELE_BINDER_TYPED) {
      return new AyaPsiTeleBinderTypedImpl(node);
    } else if (type == TELE_EX) {
      return new AyaPsiTeleExImpl(node);
    } else if (type == TELE_IM) {
      return new AyaPsiTeleImImpl(node);
    } else if (type == TELE_LIT) {
      return new AyaPsiTeleLitImpl(node);
    } else if (type == TELE_PARAM_NAME) {
      return new AyaPsiTeleParamNameImpl(node);
    } else if (type == THIS_EXPR) {
      return new AyaPsiThisExprImpl(node);
    } else if (type == TIGHTERS) {
      return new AyaPsiTightersImpl(node);
    } else if (type == TUPLE_IM_ARGUMENT) {
      return new AyaPsiTupleImArgumentImpl(node);
    } else if (type == TYPE) {
      return new AyaPsiTypeImpl(node);
    } else if (type == ULIFT_PREFIX) {
      return new AyaPsiUliftPrefixImpl(node);
    } else if (type == UNIV_EXPR) {
      return new AyaPsiUnivExprImpl(node);
    } else if (type == USE_AS) {
      return new AyaPsiUseAsImpl(node);
    } else if (type == USE_HIDE) {
      return new AyaPsiUseHideImpl(node);
    } else if (type == USE_ID) {
      return new AyaPsiUseIdImpl(node);
    } else if (type == USE_IDS_COMMA) {
      return new AyaPsiUseIdsCommaImpl(node);
    } else if (type == USE_LIST) {
      return new AyaPsiUseListImpl(node);
    } else if (type == WEAK_ID) {
      return new AyaPsiWeakIdImpl(node);
    }
    throw new AssertionError("Unknown element type: " + type);
  }
}
