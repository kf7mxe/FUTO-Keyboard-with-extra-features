/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.futo.inputmethod.keyboard.layout.tests;

import android.test.suitebuilder.annotation.Suppress;

import org.futo.inputmethod.keyboard.layout.LayoutBase;
import org.futo.inputmethod.keyboard.layout.Qwerty;
import org.futo.inputmethod.keyboard.layout.Symbols;
import org.futo.inputmethod.keyboard.layout.SymbolsShifted;
import org.futo.inputmethod.keyboard.layout.customizer.LayoutCustomizer;
import org.futo.inputmethod.keyboard.layout.expected.ExpectedKey;

import java.util.Locale;

/*
 * hi_ZZ: Hinglish/qwerty
 */
@Suppress
public final class TestsHinglish extends LayoutTestsBase {
    private static final Locale LOCALE = new Locale("hi", "ZZ");
    private static final LayoutBase LAYOUT = new Qwerty(new HinglishCustomizer(LOCALE));

    @Override
    LayoutBase getLayout() { return LAYOUT; }

    private static class HinglishCustomizer extends LayoutCustomizer {
        HinglishCustomizer(final Locale locale) { super(locale); }

        @Override
        public ExpectedKey getCurrencyKey() { return CURRENCY_RUPEE; }

        @Override
        public ExpectedKey[] getOtherCurrencyKeys() {
            return SymbolsShifted.CURRENCIES_OTHER_GENERIC;
        }

        // U+20B9: "₹" INDIAN RUPEE SIGN
        private static final ExpectedKey CURRENCY_RUPEE = key("\u20B9",
                Symbols.CURRENCY_GENERIC_MORE_KEYS);
    }
}
