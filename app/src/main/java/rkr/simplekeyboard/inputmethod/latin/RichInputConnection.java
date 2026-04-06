package rkr.simplekeyboard.inputmethod.latin;
import static android.content.ClipDescription.MIMETYPE_TEXT_HTML;
import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.SurroundingText;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;
import rkr.simplekeyboard.inputmethod.latin.settings.SpacingAndPunctuations;
import rkr.simplekeyboard.inputmethod.latin.utils.CapsModeUtils;
public final class RichInputConnection {
    private static final String TAG = "RichInputConnection";
    private static final int INVALID_CURSOR_POSITION = -1;
        private int mExpectedSelStart = INVALID_CURSOR_POSITION;
        private int mExpectedSelEnd = INVALID_CURSOR_POSITION;
        private String mTextBeforeCursor = "";
    private String mTextAfterCursor = "";
    private String mTextSelection = "";
    private final LatinIME mLatinIME;
    private InputConnection mIC;
    private int mNestLevel;
    private final ExecutorService mBackgroundThread;
    public RichInputConnection(final LatinIME latinIME) {
        mLatinIME = latinIME;
        mIC = null;
        mNestLevel = 0;
        mBackgroundThread = Executors.newSingleThreadExecutor();
    }
    public boolean isConnected() {
        return mIC != null;
    }
    public void beginBatchEdit() {
        if (++mNestLevel == 1) {
            mIC = mLatinIME.getCurrentInputConnection();
            if (isConnected()) {
                mIC.beginBatchEdit();
            }
        } else {
            Log.e(TAG, "Nest level too deep : " + mNestLevel);
        }
    }
    public void endBatchEdit() {
        if (mNestLevel <= 0) Log.e(TAG, "Batch edit not in progress!");
        if (--mNestLevel == 0 && isConnected()) {
            mIC.endBatchEdit();
        }
    }
    public void updateSelection(final int newSelStart, final int newSelEnd) {
        mExpectedSelStart = newSelStart;
        mExpectedSelEnd = newSelEnd;
    }
    @TargetApi(Build.VERSION_CODES.S)
    private void setTextAroundCursor(final SurroundingText textAroundCursor) {
        if (null == textAroundCursor) {
            Log.e(TAG, "Unable get text around cursor.");
            mTextBeforeCursor = "";
            mTextAfterCursor = "";
            mTextSelection = "";
            return;
        }
        final CharSequence text = textAroundCursor.getText();
        mTextBeforeCursor = text.subSequence(0, textAroundCursor.getSelectionStart()).toString();
        mTextSelection = text.subSequence(textAroundCursor.getSelectionStart(), textAroundCursor.getSelectionEnd()).toString();
        mTextAfterCursor = text.subSequence(textAroundCursor.getSelectionEnd(), text.length()).toString();
    }
        public void reloadTextCache(final EditorInfo editorInfo, final boolean restarting) {
        mIC = mLatinIME.getCurrentInputConnection();
        if (mExpectedSelStart != INVALID_CURSOR_POSITION && mExpectedSelEnd != INVALID_CURSOR_POSITION
            && !restarting) {
            return;
        }
        updateSelection(editorInfo.initialSelStart, editorInfo.initialSelEnd);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            final SurroundingText textAroundCursor = editorInfo
                    .getInitialSurroundingText(Constants.EDITOR_CONTENTS_CACHE_SIZE, Constants.EDITOR_CONTENTS_CACHE_SIZE, 0);
            setTextAroundCursor(textAroundCursor);
            mLatinIME.mHandler.postUpdateShiftState();
        } else {
            reloadTextCache();
        }
    }
        public void reloadTextCache() {
        mIC = mLatinIME.getCurrentInputConnection();
        if (!isConnected()) {
            return;
        }
        final int expectedSelStart = mExpectedSelStart;
        final int expectedSelEnd = mExpectedSelEnd;
        mBackgroundThread.execute(() -> {
            if (!isConnected()) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                final SurroundingText textAroundCursor =
                        mIC.getSurroundingText(Constants.EDITOR_CONTENTS_CACHE_SIZE, Constants.EDITOR_CONTENTS_CACHE_SIZE, 0);
                if (expectedSelStart != mExpectedSelStart || expectedSelEnd != mExpectedSelEnd) {
                    Log.w(TAG, "Selection range modified before thread completion.");
                    return;
                }
                setTextAroundCursor(textAroundCursor);
                mLatinIME.mHandler.postUpdateShiftState();
            } else {
                final CharSequence textBeforeCursor = mIC.getTextBeforeCursor(Constants.EDITOR_CONTENTS_CACHE_SIZE, 0);
                if (expectedSelStart != mExpectedSelStart) {
                    Log.w(TAG, "Selection start modified before thread completion.");
                    return;
                }
                if (null == textBeforeCursor) {
                    Log.e(TAG, "Unable get text before cursor.");
                    mTextBeforeCursor = "";
                    return;
                } else {
                    mTextBeforeCursor = textBeforeCursor.toString();
                }
                mLatinIME.mHandler.postUpdateShiftState();
                final CharSequence textAfterCursor = mIC.getTextAfterCursor(Constants.EDITOR_CONTENTS_CACHE_SIZE, 0);
                if (expectedSelEnd != mExpectedSelEnd) {
                    Log.w(TAG, "Selection end modified before thread completion.");
                    return;
                }
                if (null == textAfterCursor) {
                    Log.e(TAG, "Unable get text after cursor.");
                    mTextAfterCursor = "";
                } else {
                    mTextAfterCursor = textAfterCursor.toString();
                }
                if (hasSelection()) {
                    final CharSequence textSelection = mIC.getSelectedText(0);
                    if (expectedSelStart != mExpectedSelStart || expectedSelEnd != mExpectedSelEnd) {
                        Log.w(TAG, "Selection range modified before thread completion.");
                        return;
                    }
                    if (null == textSelection) {
                        Log.e(TAG, "Unable get text selection.");
                        mTextSelection = "";
                    } else {
                        mTextSelection = textSelection.toString();
                    }
                } else {
                    mTextSelection = "";
                }
            }
        });
    }
    public void clearCaches() {
        Log.i(TAG, "Clearing text caches.");
        mExpectedSelStart = INVALID_CURSOR_POSITION;
        mExpectedSelEnd = INVALID_CURSOR_POSITION;
        mTextBeforeCursor = "";
        mTextSelection = "";
        mTextAfterCursor = "";
    }
        public void commitText(final CharSequence text, final int newCursorPosition) {
        RichInputMethodManager.getInstance().resetSubtypeCycleOrder();
        mTextBeforeCursor += text;
        if (hasCursorPosition()) {
            mExpectedSelStart += text.length();
            mExpectedSelEnd = mExpectedSelStart;
        }
        if (isConnected()) {
            mIC.commitText(text, newCursorPosition);
        }
    }
    public CharSequence getSelectedText() {
        return mTextSelection;
    }
    public boolean canDeleteCharacters() {
        return mExpectedSelStart > 0;
    }
        public int getCursorCapsMode(final int inputType, final SpacingAndPunctuations spacingAndPunctuations) {
        mIC = mLatinIME.getCurrentInputConnection();
        if (!isConnected()) {
            return Constants.TextUtils.CAP_MODE_OFF;
        }
        return CapsModeUtils.getCapsMode(mTextBeforeCursor, inputType,
                spacingAndPunctuations);
    }
    public int getCodePointBeforeCursor() {
        final int length = mTextBeforeCursor.length();
        if (length < 1) return Constants.NOT_A_CODE;
        return Character.codePointBefore(mTextBeforeCursor, length);
    }
    public void replaceText(final int startPosition, final int endPosition, CharSequence text) {
        if (mExpectedSelStart != mExpectedSelEnd) {
            Log.e(TAG, "replaceText called with text range selected");
            return;
        }
        if (mExpectedSelStart != startPosition) {
            Log.e(TAG, "replaceText called with range not starting with current cursor position");
            return;
        }
        final int numCharsSelected = endPosition - startPosition;
        final String textAfterCursor = mTextAfterCursor;
        if (textAfterCursor.length() < numCharsSelected) {
            Log.e(TAG, "replaceText called with range longer than current text");
            return;
        }
        mTextAfterCursor = text + textAfterCursor.substring(numCharsSelected);
        RichInputMethodManager.getInstance().resetSubtypeCycleOrder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            mIC.replaceText(startPosition, endPosition, text, 0, null);
        } else {
            mIC.deleteSurroundingText(0, numCharsSelected);
            mIC.commitText(text, 0);
        }
    }
    public void deleteTextBeforeCursor(final int numChars) {
        String textBeforeCursor = mTextBeforeCursor;
        if (!textBeforeCursor.isEmpty() && textBeforeCursor.length() >= numChars) {
            mTextBeforeCursor = textBeforeCursor.substring(0, textBeforeCursor.length() - numChars);
        }
        if (mExpectedSelStart >= numChars) {
            mExpectedSelStart -= numChars;
        }
        mIC.deleteSurroundingText(numChars, 0);
    }
    public void deleteSelectedText() {
        if (mExpectedSelStart == mExpectedSelEnd) {
            Log.e(TAG, "deleteSelectedText called with text range not selected");
            return;
        }
        beginBatchEdit();
        final int selectionLength = mExpectedSelEnd - mExpectedSelStart;
        mTextSelection = "";
        setSelection(mExpectedSelStart, mExpectedSelStart);
        mIC.deleteSurroundingText(0, selectionLength);
        endBatchEdit();
    }
    public void performEditorAction(final int actionId) {
        mIC = mLatinIME.getCurrentInputConnection();
        if (isConnected()) {
            mIC.performEditorAction(actionId);
        }
    }
    public void pasteClipboard() {
        final ClipboardManager clipboard = (ClipboardManager) mLatinIME.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null && clipboard.hasPrimaryClip()) {
            final ClipData clipData = clipboard.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() == 1) {
                final String mimeType = clipData.getDescription().getMimeType(0);
                if (MIMETYPE_TEXT_PLAIN.equals(mimeType) || MIMETYPE_TEXT_HTML.equals(mimeType)) {
                    final CharSequence pasteData = clipData.getItemAt(0).getText();
                    if (pasteData != null && pasteData.length() > 0) {
                        mLatinIME.onTextInput(pasteData.toString());
                        return;
                    }
                }
            }
        }
        mIC.performContextMenuAction(android.R.id.paste);
    }
    public void sendKeyEvent(final KeyEvent keyEvent) {
        RichInputMethodManager.getInstance().resetSubtypeCycleOrder();
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_ENTER:
                mTextBeforeCursor += "\n";
                if (hasCursorPosition()) {
                    mExpectedSelStart += 1;
                    mExpectedSelEnd = mExpectedSelStart;
                }
                break;
            case KeyEvent.KEYCODE_UNKNOWN:
                if (null != keyEvent.getCharacters()) {
                    mTextBeforeCursor += keyEvent.getCharacters();
                    if (hasCursorPosition()) {
                        mExpectedSelStart += keyEvent.getCharacters().length();
                        mExpectedSelEnd = mExpectedSelStart;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DEL:
                break;
            default:
                final String text = StringUtils.newSingleCodePointString(keyEvent.getUnicodeChar());
                mTextBeforeCursor += text;
                if (hasCursorPosition()) {
                    mExpectedSelStart += text.length();
                    mExpectedSelEnd = mExpectedSelStart;
                }
                break;
            }
        }
        if (isConnected()) {
            mIC.sendKeyEvent(keyEvent);
        }
    }
        public void setSelection(int start, int end) {
        if (start < 0 || end < 0 || start > end) {
            return;
        }
        if (mExpectedSelStart == start && mExpectedSelEnd == end) {
            return;
        }
        final int textStart = mExpectedSelStart - mTextBeforeCursor.length();
        final String textRange = mTextBeforeCursor + mTextSelection + mTextAfterCursor;
        if (textRange.length() >= end - textStart && start - textStart >= 0 && textStart >= 0) {
            mTextBeforeCursor = textRange.substring(0, start - textStart);
            mTextSelection = textRange.substring(start - textStart, end - textStart);
            mTextAfterCursor = textRange.substring(end - textStart);
        }
        RichInputMethodManager.getInstance().resetSubtypeCycleOrder();
        mExpectedSelStart = start;
        mExpectedSelEnd = end;
        if (isConnected()) {
            mIC.setSelection(start, end);
        }
    }
    public int getExpectedSelectionStart() {
        return mExpectedSelStart;
    }
    public int getExpectedSelectionEnd() {
        return mExpectedSelEnd;
    }
        public boolean hasSelection() {
        return mExpectedSelEnd != mExpectedSelStart;
    }
    public boolean hasCursorPosition() {
        return mExpectedSelStart != INVALID_CURSOR_POSITION && mExpectedSelEnd != INVALID_CURSOR_POSITION;
    }
        public int getUnicodeSteps(int chars, boolean rightSidePointer) {
        int steps = 0;
        if (chars < 0) {
            CharSequence charsBeforeCursor = rightSidePointer && hasSelection() ?
                    getSelectedText() :
                    mTextBeforeCursor;
            if (charsBeforeCursor == null || charsBeforeCursor == "") {
                return chars;
            }
            for (int i = charsBeforeCursor.length() - 1; i >= 0 && chars < 0; i--, steps--) {
                if (i > 1 && charsBeforeCursor.charAt(i - 1) == '\u200d') {
                    continue;
                }
                if (charsBeforeCursor.charAt(i) == '\u200d') {
                    continue;
                }
                if (Character.isSurrogate(charsBeforeCursor.charAt(i)) &&
                        !Character.isHighSurrogate(charsBeforeCursor.charAt(i))) {
                    continue;
                }
                chars++;
            }
        } else if (chars > 0) {
            CharSequence charsAfterCursor = !rightSidePointer && hasSelection() ?
                    getSelectedText() :
                    mTextAfterCursor;
            if (charsAfterCursor == null || charsAfterCursor == "") {
                return chars;
            }
            for (int i = 0; i < charsAfterCursor.length() && chars > 0; i++, steps++) {
                if (i < charsAfterCursor.length() - 1 && charsAfterCursor.charAt(i + 1) == '\u200d') {
                    continue;
                }
                if (charsAfterCursor.charAt(i) == '\u200d') {
                    continue;
                }
                if (Character.isHighSurrogate(charsAfterCursor.charAt(i))) {
                    continue;
                }
                chars--;
            }
        }
        return steps;
    }
}
