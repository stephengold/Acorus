/*
 Copyright (c) 2013-2022, Stephen Gold
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. Neither the name of the copyright holder nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package jme3utilities.ui;

import com.jme3.input.InputManager;
import com.jme3.input.Joystick;
import com.jme3.input.JoystickButton;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.JoyButtonTrigger;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.controls.Trigger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jme3utilities.Heart;
import jme3utilities.MyString;
import jme3utilities.Validate;

/**
 * A named, immutable, simple trigger for actions.
 *
 * @author Stephen Gold sgold@sonic.net
 */
final public class Hotkey {
    // *************************************************************************
    // constants and loggers

    /**
     * maximum number of buttons per joystick
     */
    final private static int maxButtonsPerJoystick = 12;
    /**
     * maximum number of buttons on the mouse
     */
    final private static int maxMouseButtons = 3;
    /**
     * universal code for the first mouse button
     */
    final private static int firstMouseButton = KeyInput.KEY_LAST + 1;
    /**
     * universal code for the last mouse button
     */
    final private static int lastMouseButton = KeyInput.KEY_LAST + maxMouseButtons;
    /**
     * universal code for the first joystick button
     */
    final private static int firstJoystickButton = lastMouseButton + 1;
    /**
     * message logger for this class
     */
    final private static Logger logger
            = Logger.getLogger(Hotkey.class.getName());
    // *************************************************************************
    // fields

    /**
     * input manager of the Application
     */
    private static InputManager inputManager;
    /**
     * universal code of this hotkey: either
     * <p>
     * a JME key code (from {@link com.jme3.input.KeyInput}) or
     * <p>
     * firstMouseButton + a JME button code (from
     * {@link com.jme3.input.MouseInput}) or
     * <p>
     * {@code firstJoystickButton + maxButtonsPerJoystick * joystickIndex + buttonIndex}
     */
    final private int universalCode;
    /**
     * map universal codes to hotkeys
     */
    final private static Map<Integer, Hotkey> byUniversalCode = new TreeMap<>();
    /**
     * map local names to hotkeys
     */
    final private static Map<String, Hotkey> byLocalName = new TreeMap<>();
    /**
     * map US names to hotkeys
     */
    final private static Map<String, Hotkey> byUsName = new TreeMap<>();
    /**
     * brief, descriptive name of this hotkey (not null, not empty) for use by
     * BindScreen and HelpUtils. On systems with Dvorak or non-US keyboards,
     * this might differ from its US name, but only if LWJGL v3 is used. When
     * LWJGL v3 not used, localization doesn't take place and
     * localName.equals(usName).
     */
    final private String localName;
    /**
     * brief, descriptive name of this hotkey on systems with United States
     * QWERTY keyboards (not null, not empty). This is the name InputMode uses.
     */
    final private String usName;
    /**
     * JME input trigger of this hotkey (not null)
     */
    final private Trigger trigger;
    // *************************************************************************
    // constructors

    /**
     * Instantiate a Hotkey with the specified universal code, local name, US
     * name, and trigger.
     *
     * @param universalCode the desired universal code
     * @param localName the desired local name (not null, not empty)
     * @param usName the desired US name (not null, not empty)
     * @param trigger the desired trigger (not null)
     */
    private Hotkey(int universalCode, String localName, String usName,
            Trigger trigger) {
        assert universalCode >= 0 : universalCode;
        assert localName != null;
        assert !localName.isEmpty();
        assert usName != null;
        assert !usName.isEmpty();
        assert trigger != null;

        this.universalCode = universalCode;
        this.localName = localName;
        this.usName = usName;
        this.trigger = trigger;
    }
    // *************************************************************************
    // new methods exposed

    /**
     * Determine the button code of this mouse-button hotkey.
     *
     * @return a JME button code (from {@link com.jme3.input.MouseInput}) or -1
     * if none
     */
    public int buttonCode() {
        int buttonCode;
        if (universalCode < firstMouseButton
                || universalCode > lastMouseButton) {
            buttonCode = -1;
        } else {
            buttonCode = universalCode - firstMouseButton;
        }

        assert buttonCode >= -1 : buttonCode;
        assert buttonCode < maxMouseButtons : buttonCode;
        return buttonCode;
    }

    /**
     * Determine the universal code of this hotkey.
     *
     * @return a universal code (&ge;0)
     */
    public int code() {
        assert universalCode >= 0 : universalCode;
        return universalCode;
    }

    /**
     * Find a hotkey by its universal code.
     *
     * @param universalCode a universal code: either a key code (from
     * {@link com.jme3.input.KeyInput}) or firstMouseButton + a mouse-button
     * code (from {@link com.jme3.input.MouseInput}) or joystick-button code
     * (computed from a joystick index and a joystick-button index)
     * @return the pre-existing instance (or null if none)
     */
    public static Hotkey find(int universalCode) {
        Validate.nonNegative(universalCode, "universal code");
        Hotkey result = byUniversalCode.get(universalCode);
        return result;
    }

    /**
     * Find a mouse-button hotkey by its button code.
     *
     * @param buttonCode a JME button code from
     * {@link com.jme3.input.MouseInput}
     * @return the pre-existing instance (or null if none)
     */
    public static Hotkey findButton(int buttonCode) {
        Validate.inRange(buttonCode, "button code", 0, maxMouseButtons - 1);
        Hotkey result = find(firstMouseButton + buttonCode);
        return result;
    }

    /**
     * Find a hotkey by its key code.
     *
     * @param keyCode a key code from {@link com.jme3.input.KeyInput}
     * @return the pre-existing instance (or null if none)
     */
    public static Hotkey findKey(int keyCode) {
        Validate.inRange(keyCode, "key code", 0, KeyInput.KEY_LAST);
        Hotkey result = find(keyCode);
        return result;
    }

    /**
     * Find a hotkey by its local name.
     *
     * @param localName a local name (not null, not empty)
     * @return the pre-existing instance (or null if none)
     */
    public static Hotkey findLocal(String localName) {
        Validate.nonEmpty(localName, "local name");
        Hotkey result = byLocalName.get(localName);
        return result;
    }

    /**
     * Find a hotkey by its US name.
     *
     * @param usName a US name (not null, not empty)
     * @return the pre-existing instance (or null if none)
     */
    public static Hotkey findUs(String usName) {
        Validate.nonEmpty(usName, "US name");
        Hotkey result = byUsName.get(usName);
        return result;
    }

    /**
     * Instantiate all known hotkeys.
     *
     * @param inputManager the application's input manager (not null)
     */
    static void initialize(InputManager inputManager) {
        assert inputManager != null;
        Hotkey.inputManager = inputManager;
        /*
         * mouse buttons
         */
        addMouseButton(MouseInput.BUTTON_LEFT, "LMB");
        addMouseButton(MouseInput.BUTTON_MIDDLE, "MMB");
        addMouseButton(MouseInput.BUTTON_RIGHT, "RMB");

        initializeKeys();
        /*
         * joystick buttons, if any
         */
        Joystick[] sticks = inputManager.getJoysticks();
        if (sticks != null) {
            for (Joystick joystick : sticks) {
                int joyIndex = joystick.getJoyId();
                List<JoystickButton> buttons = joystick.getButtons();
                for (JoystickButton button : buttons) {
                    int buttonIndex = button.getButtonId();
                    addJoystickButton(joyIndex, buttonIndex);
                }
            }
        }
    }

    /**
     * Determine the JME key code of this hotkey.
     *
     * @return a JME key code (from {@link com.jme3.input.KeyInput}) or -1 if
     * none
     */
    public int keyCode() {
        int keyCode;
        if (universalCode < firstMouseButton) {
            keyCode = universalCode;
        } else {
            keyCode = -1;
        }

        assert keyCode >= -1 : keyCode;
        assert keyCode <= KeyInput.KEY_LAST : keyCode;
        return keyCode;
    }

    /**
     * Enumerate all known hotkeys.
     *
     * @return a new list
     */
    public static List<Hotkey> listAll() {
        Collection<Hotkey> all = byLocalName.values();
        int numInstances = all.size();
        List<Hotkey> result = new ArrayList<>(numInstances);
        result.addAll(all);

        return result;
    }

    /**
     * Determine the local name of this hotkey, which is the name used by
     * BindScreen and HelpUtils.
     *
     * @return the local name (not null, not empty)
     */
    public String localName() {
        assert localName != null;
        assert !localName.isEmpty();
        return localName;
    }

    /**
     * Map this hotkey to an action string in the input manager. Overrides any
     * previous mappings for the hotkey.
     *
     * @param actionString action string (not null)
     */
    void map(String actionString) {
        Validate.nonNull(actionString, "action string");
        inputManager.addMapping(actionString, trigger);
    }

    /**
     * Unmap this hotkey in the input manager.
     *
     * @param actionString action string (not null)
     */
    void unmap(String actionString) {
        Validate.nonNull(actionString, "action string");

        if (inputManager.hasMapping(actionString)) {
            inputManager.deleteTrigger(actionString, trigger);
        }
    }

    /**
     * Determine the US name of this hotkey, which is the name InputMode uses.
     *
     * @return the brief, descriptive name for this hotkey on systems with
     * United States QWERTY keyboards (not null, not empty)
     */
    public String usName() {
        assert usName != null;
        assert !usName.isEmpty();
        return usName;
    }
    // *************************************************************************
    // private methods

    /**
     * Add a new hotkey for a joystick button.
     *
     * @param joystickIndex the JME joystick index (&ge;0)
     * @param buttonIndex the JME button index within the joystick (&ge;0,
     * &lt;12)
     */
    private static void addJoystickButton(int joystickIndex, int buttonIndex) {
        assert joystickIndex >= 0 : joystickIndex;
        assert buttonIndex >= 0 : buttonIndex;
        assert buttonIndex < maxButtonsPerJoystick : buttonIndex;

        String name = String.format("j%d.b%d", joystickIndex, buttonIndex);
        int universalCode = firstJoystickButton
                + maxButtonsPerJoystick * joystickIndex + buttonIndex;
        assert find(universalCode) == null :
                name + " is already assigned to a hotkey";
        assert findLocal(name) == null;
        assert findUs(name) == null;

        Trigger trigger = new JoyButtonTrigger(joystickIndex, buttonIndex);
        Hotkey instance = new Hotkey(universalCode, name, name, trigger);

        byUniversalCode.put(universalCode, instance);
        byLocalName.put(name, instance);
        byUsName.put(name, instance);
    }

    /**
     * Add a new hotkey for a mouse button.
     *
     * @param buttonCode the JME mouse-button code (from
     * {@link com.jme3.input.MouseInput}) that isn't already assigned to a
     * hotkey
     * @param name a name not already assigned (not null, not empty)
     */
    private static void addMouseButton(int buttonCode, String name) {
        assert buttonCode >= 0 : buttonCode;
        assert buttonCode < maxMouseButtons : buttonCode;
        assert name != null;
        assert !name.isEmpty();
        assert findButton(buttonCode) == null :
                "button" + buttonCode + " is already assigned to a hotkey";
        assert findLocal(name) == null;
        assert findUs(name) == null;

        int universalCode = firstMouseButton + buttonCode;
        Trigger trigger = new MouseButtonTrigger(buttonCode);
        Hotkey instance = new Hotkey(universalCode, name, name, trigger);

        byUniversalCode.put(universalCode, instance);
        byLocalName.put(name, instance);
        byUsName.put(name, instance);
    }

    /**
     * Add a hotkey for a keyboard key.
     *
     * @param keyCode the JME key code from {@link com.jme3.input.KeyInput} that
     * isn't already assigned to a hotkey
     * @param usName the name of the key on United States QWERTY keyboards (not
     * null, not empty)
     */
    private static void addKey(int keyCode, String usName) {
        assert keyCode >= 0 : keyCode;
        assert keyCode <= KeyInput.KEY_LAST : keyCode;
        assert findKey(keyCode) == null :
                "key" + keyCode + " is already assigned to a hotkey";
        assert usName != null;
        assert !usName.isEmpty();
        /*
         * Attempt to localize the name for HelpUtils and BindScreen.
         */
        String localName = usName;
        if (!usName.startsWith("numpad ")) { // not a numpad key
            String glfwName = null;
            try {
                glfwName = inputManager.getKeyName(keyCode);
            } catch (UnsupportedOperationException exception) {
                // probably using LWJGL v2
            }

            if (glfwName != null) { // key is printable
                localName = englishName(glfwName);

                if (!localName.equals(usName)) {
                    String usQ = MyString.quote(usName);
                    String localQ = MyString.quote(localName);
                    if (localName.length() == 1) {
                        int ch = localName.charAt(0);
                        String unicodeName = Character.getName(ch);
                        localQ += String.format("    (\"\\u%04x\": %s)",
                                ch, unicodeName);
                    }
                    logger.log(Level.INFO,
                            "localizing the hotkey name for key{0}: {1} -> {2}",
                            new Object[]{keyCode, usQ, localQ});
                }
            }
        }
        /*
         * In case of a duplicate local name (such as "circumflex"), the hotkey with
         * the localized name is preferred.  If both hotkeys have localized
         * names, the new hotkey overrides the pre-existing one.
         */
        Hotkey preexistingHotkey = findLocal(localName);
        if (preexistingHotkey != null) {
            int preexistingCode = preexistingHotkey.keyCode();
            String nameQ = MyString.quote(usName);
            Object[] args = {keyCode, preexistingCode, nameQ};
            if (localName.equals(usName)) {
                logger.log(Level.INFO,
                        "Ignore key{0} because pre-existing key{1} is "
                        + "also named {2}.", args);
                return;
            } else {
                logger.log(Level.INFO,
                        "Key{0} overrides pre-existing key{1} that was "
                        + "also named {2}.", args);

                byLocalName.remove(localName);
                byUniversalCode.remove(preexistingCode);
            }
        }

        int universalCode = keyCode;
        Trigger trigger = new KeyTrigger(keyCode);
        Hotkey instance = new Hotkey(universalCode, localName, usName, trigger);

        byUniversalCode.put(universalCode, instance);
        byLocalName.put(localName, instance);
        byUsName.put(usName, instance);
    }

    /**
     * Transform the GLFW name of a printable keyboard key into a brief,
     * descriptive name in English. Only a few common names are handled. When a
     * name isn't handled, the GLFW name is returned. TODO handle additional
     * cases
     *
     * @param glfwKeyName a key name obtained from GLFW (not null, typically a
     * single Unicode character)
     * @return a brief, descriptive name for the key (not null)
     */
    private static String englishName(String glfwKeyName) {
        assert glfwKeyName != null;

        switch (glfwKeyName) {
            case "\u0430":
                return "a";
            case "\u00B4":
                return "acute";
            case "\u03B1":
                return "alpha";
            case "&":
                return "ampersand";
            case "'":
                return "apostrophe";
            case "\\":
                return "backslash";
            case "`":
                return "backtick";
            case "\u0431":
                return "be";
            case "β":
                return "beta";
            case "\u0447":
                return "che";
            case "\u03C7":
                return "chi";
            case "^":
                return "circumflex";
            case ":":
                return "colon";
            case ",":
                return "comma";
            case "\u0434":
                return "de";
            case "δ":
                return "delta";
            case "\u00A8":
                return "diaeresis";
            case "$":
                return "dollar";
            case "\u044D":
                return "e";
            case "\u0444":
                return "ef";
            case "\u043B":
                return "el";
            case "\u043C":
                return "em";
            case "\u043D":
                return "en";
            case "ε":
                return "epsilon";
            case "=":
                return "equals";
            case "\u0440":
                return "er";
            case "\u0441":
                return "es";
            case "η":
                return "eta";
            case "!":
                return "exclaim";
            case "\u03C2":
                return "fin sigma";
            case "\u03B3":
                return "gamma";
            case "\u0433":
                return "ghe";
            case "\u0445":
                return "ha";
            case "½":
                return "half";
            case "\u044A":
                return "hard";
            case "#":
                return "hash";
            case "\u0438":
                return "i";
            case "\u0435":
                return "ie";
            case "\u00A1":
                return "inv exclaim";
            case "\u0451":
                return "io";
            case "\u03B9":
                return "iota";
            case "\u043A":
                return "ka";
            case "\u03BA":
                return "kappa";
            case "λ":
                return "lambda";
            case "[":
                return "left bracket";
            case "(":
                return "left paren";
            case "<":
                return "less than";
            case "µ":
                return "micro";
            case "-":
                return "minus";
            case "\u03BC":
                return "mu";
            case "\u03BD":
                return "nu";
            case "\u043E":
                return "o";
            case "ω":
                return "omega";
            case "\u03BF":
                return "omicron";
            case "\u00BA":
                return "ordinal";
            case "\u043F":
                return "pe";
            case ".":
                return "period";
            case "\u03C6":
                return "phi";
            case "\u03C0":
                return "pi";
            case "+":
                return "plus";
            case "ψ":
                return "psi";
            case "\"":
                return "quote";
            case "\u03C1":
                return "rho";
            case "]":
                return "right bracket";
            case ")":
                return "right paren";
            case "§":
                return "section";
            case ";":
                return "semicolon";
            case "\u0448":
                return "sha";
            case "\u0449":
                return "shcha";
            case "\u0439":
                return "short i";
            case "σ":
                return "sigma";
            case "/":
                return "slash";
            case "\u044C":
                return "soft";
            case "²":
                return "super2";
            case "\u03C4":
                return "tau";
            case "\u0442":
                return "te";
            case "θ":
                return "theta";
            case "\u0384":
                return "tonos";
            case "\u0446":
                return "tse";
            case "\u0443":
                return "u";
            case "\u03C5":
                return "upsilon";
            case "\u0432":
                return "ve";
            case "ξ":
                return "xi";
            case "\u044F":
                return "ya";
            case "\u044B":
                return "yeru";
            case "\u044E":
                return "yu";
            case "\u0437":
                return "ze";
            case "\u03B6":
                return "zeta";
            case "\u0436":
                return "zhe";
            default:
                return glfwKeyName;
        }
    }

    /**
     * Instantiate hotkeys for all known keyboard keys.
     */
    private static void initializeKeys() {
        KeyInput keyInput = Heart.getKeyInput(inputManager);
        String keyInputClassName = keyInput.getClass().getSimpleName();
        if (keyInputClassName.equals("DummyKeyInput")) {
            return; // probably in a Headless context
        }
        /*
         * mode keys
         */
        addKey(KeyInput.KEY_LCONTROL, "left ctrl");
        addKey(KeyInput.KEY_LMENU, "left alt");
        addKey(KeyInput.KEY_LMETA, "left meta");
        addKey(KeyInput.KEY_LSHIFT, "left shift");

        addKey(KeyInput.KEY_RCONTROL, "right ctrl");
        addKey(KeyInput.KEY_RMENU, "right alt");
        addKey(KeyInput.KEY_RMETA, "right meta");
        addKey(KeyInput.KEY_RSHIFT, "right shift");

        addKey(KeyInput.KEY_CAPITAL, "caps lock");
        /*
         * main keyboard letters
         */
        addKey(KeyInput.KEY_A, "a");
        addKey(KeyInput.KEY_B, "b");
        addKey(KeyInput.KEY_C, "c");
        addKey(KeyInput.KEY_D, "d");
        addKey(KeyInput.KEY_E, "e");
        addKey(KeyInput.KEY_F, "f");
        addKey(KeyInput.KEY_G, "g");
        addKey(KeyInput.KEY_H, "h");
        addKey(KeyInput.KEY_I, "i");
        addKey(KeyInput.KEY_J, "j");
        addKey(KeyInput.KEY_K, "k");
        addKey(KeyInput.KEY_L, "l");
        addKey(KeyInput.KEY_M, "m");
        addKey(KeyInput.KEY_N, "n");
        addKey(KeyInput.KEY_O, "o");
        addKey(KeyInput.KEY_P, "p");
        addKey(KeyInput.KEY_Q, "q");
        addKey(KeyInput.KEY_R, "r");
        addKey(KeyInput.KEY_S, "s");
        addKey(KeyInput.KEY_T, "t");
        addKey(KeyInput.KEY_U, "u");
        addKey(KeyInput.KEY_V, "v");
        addKey(KeyInput.KEY_W, "w");
        addKey(KeyInput.KEY_X, "x");
        addKey(KeyInput.KEY_Y, "y");
        addKey(KeyInput.KEY_Z, "z");
        /*
         * main keyboard digits
         */
        addKey(KeyInput.KEY_1, "1");
        addKey(KeyInput.KEY_2, "2");
        addKey(KeyInput.KEY_3, "3");
        addKey(KeyInput.KEY_4, "4");
        addKey(KeyInput.KEY_5, "5");
        addKey(KeyInput.KEY_6, "6");
        addKey(KeyInput.KEY_7, "7");
        addKey(KeyInput.KEY_8, "8");
        addKey(KeyInput.KEY_9, "9");
        addKey(KeyInput.KEY_0, "0");
        /*
         * main keyboard punctuation
         */
        addKey(KeyInput.KEY_GRAVE, "backtick");
        addKey(KeyInput.KEY_MINUS, "minus");
        addKey(KeyInput.KEY_EQUALS, "equals");
        addKey(KeyInput.KEY_LBRACKET, "left bracket");
        addKey(KeyInput.KEY_RBRACKET, "right bracket");
        addKey(KeyInput.KEY_BACKSLASH, "backslash");
        addKey(KeyInput.KEY_SEMICOLON, "semicolon");
        addKey(KeyInput.KEY_APOSTROPHE, "apostrophe");
        addKey(KeyInput.KEY_COMMA, "comma");
        addKey(KeyInput.KEY_PERIOD, "period");
        addKey(KeyInput.KEY_SLASH, "slash");
        /*
         * ASCII control and whitespace keys
         */
        addKey(KeyInput.KEY_ESCAPE, "esc");
        addKey(KeyInput.KEY_BACK, "backspace");
        addKey(KeyInput.KEY_TAB, "tab");
        addKey(KeyInput.KEY_RETURN, "enter");
        addKey(KeyInput.KEY_SPACE, "space");
        /*
         * function keys
         */
        addKey(KeyInput.KEY_F1, "f1");
        addKey(KeyInput.KEY_F2, "f2");
        addKey(KeyInput.KEY_F3, "f3");
        addKey(KeyInput.KEY_F4, "f4");
        addKey(KeyInput.KEY_F5, "f5");
        addKey(KeyInput.KEY_F6, "f6");
        addKey(KeyInput.KEY_F7, "f7");
        addKey(KeyInput.KEY_F8, "f8");
        addKey(KeyInput.KEY_F9, "f9");
        addKey(KeyInput.KEY_F10, "f10");
        addKey(KeyInput.KEY_F11, "f11");
        addKey(KeyInput.KEY_F12, "f12");
        addKey(KeyInput.KEY_F13, "f13");
        addKey(KeyInput.KEY_F14, "f14");
        addKey(KeyInput.KEY_F15, "f15");
        /*
         * editing and arrow keys
         */
        addKey(KeyInput.KEY_INSERT, "insert");
        addKey(KeyInput.KEY_HOME, "home");
        addKey(KeyInput.KEY_PGUP, "page up");
        addKey(KeyInput.KEY_DELETE, "delete");
        addKey(KeyInput.KEY_END, "end");
        addKey(KeyInput.KEY_PGDN, "page down");
        addKey(KeyInput.KEY_UP, "up arrow");
        addKey(KeyInput.KEY_LEFT, "left arrow");
        addKey(KeyInput.KEY_DOWN, "down arrow");
        addKey(KeyInput.KEY_RIGHT, "right arrow");
        /*
         * system keys
         */
        addKey(KeyInput.KEY_SYSRQ, "sys rq");
        addKey(KeyInput.KEY_SCROLL, "scroll lock");
        addKey(KeyInput.KEY_PAUSE, "pause");
        addKey(KeyInput.KEY_PRTSCR, "prtscr");
        /*
         * the numeric keypad
         */
        addKey(KeyInput.KEY_NUMLOCK, "num lock");
        addKey(KeyInput.KEY_DECIMAL, "numpad decimal");
        addKey(KeyInput.KEY_DIVIDE, "numpad divide");
        addKey(KeyInput.KEY_MULTIPLY, "numpad multiply");
        addKey(KeyInput.KEY_NUMPAD7, "numpad 7");
        addKey(KeyInput.KEY_NUMPAD8, "numpad 8");
        addKey(KeyInput.KEY_NUMPAD9, "numpad 9");
        addKey(KeyInput.KEY_ADD, "numpad add");
        addKey(KeyInput.KEY_NUMPAD4, "numpad 4");
        addKey(KeyInput.KEY_NUMPAD5, "numpad 5");
        addKey(KeyInput.KEY_NUMPAD6, "numpad 6");
        addKey(KeyInput.KEY_NUMPAD1, "numpad 1");
        addKey(KeyInput.KEY_NUMPAD2, "numpad 2");
        addKey(KeyInput.KEY_NUMPAD3, "numpad 3");
        addKey(KeyInput.KEY_NUMPADENTER, "numpad enter");
        addKey(KeyInput.KEY_NUMPAD0, "numpad 0");
        addKey(KeyInput.KEY_NUMPADCOMMA, "numpad comma");
        addKey(KeyInput.KEY_NUMPADEQUALS, "numpad equals");
        addKey(KeyInput.KEY_SUBTRACT, "numpad subtract");
        /*
         * miscellaneous keys
         *
         * None of these are listed in GlfwKeyMap, so I believe they aren't
         * needed for LWJGL v3.
         */
        boolean isV3KeyInput = keyInputClassName.equals("GlfwKeyInput");
        if (!isV3KeyInput) {
            addKey(KeyInput.KEY_APPS, "apps");
            addKey(KeyInput.KEY_AT, "at sign");
            addKey(KeyInput.KEY_AX, "ax");
            addKey(KeyInput.KEY_CIRCUMFLEX, "circumflex");
            addKey(KeyInput.KEY_COLON, "colon");
            addKey(KeyInput.KEY_CONVERT, "convert");
            addKey(KeyInput.KEY_KANA, "kana");
            addKey(KeyInput.KEY_KANJI, "kanji");
            addKey(KeyInput.KEY_NOCONVERT, "no convert");
            addKey(KeyInput.KEY_POWER, "power");
            addKey(KeyInput.KEY_SLEEP, "sleep");
            addKey(KeyInput.KEY_STOP, "stop");
            addKey(KeyInput.KEY_UNDERLINE, "underline");
            addKey(KeyInput.KEY_UNLABELED, "unlabeled");
            addKey(KeyInput.KEY_YEN, "yen");
        }
    }
}
