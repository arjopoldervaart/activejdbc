/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.javalite.activejdbc;

import org.javalite.activejdbc.test.ActiveJDBCTest;
import org.javalite.activejdbc.test_models.Page;
import org.junit.Test;

/**
 * @author igor on 7/6/14.
 */
public class CustomConverterTest extends ActiveJDBCTest {
    @Test
    public void shouldConvertStringToInteger(){
        Page p = new Page();
        p.setInteger("word_count", "zero");
        a(p.get("word_count") instanceof Integer).shouldBeTrue();
        a(p.get("word_count")).shouldBeEqual(0);
    }
}
