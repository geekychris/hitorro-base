/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.objects;

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.core.valuemap.DomainValueIntf;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

/**
 * Embeddable version of DomainValue for use in @ElementCollection.
 * This class does NOT extend BaseType (no ID field) so it can be embedded in collections.
 */
@Embeddable
public class EmbeddableDomainValue implements DomainValueIntf, Serializable {
    private static final long serialVersionUID = 1L;
    public static final String Seperator = "?";

    private String m_domain;
    private String m_value;

    public EmbeddableDomainValue() {
    }

    public EmbeddableDomainValue(String domain, String value) {
        setDomain(domain);
        setValue(value);
    }

    public static final String getConcatinated(String domain, String value) {
        return StringUtil.strcat(domain, Seperator, value);
    }

    public String getDomain() {
        return m_domain;
    }

    public void setDomain(String domain) {
        m_domain = domain;
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(String value) {
        m_value = value;
    }

    public String getAsIndexTuple() {
        return StringUtil.strcat(m_domain, Seperator, m_value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainValueIntf)) return false;
        DomainValueIntf that = (DomainValueIntf) o;
        return StringUtil.equals(m_domain, that.getDomain(), true) &&
               StringUtil.equals(m_value, that.getValue(), true);
    }

    @Override
    public int hashCode() {
        int result = m_domain != null ? m_domain.hashCode() : 0;
        result = 31 * result + (m_value != null ? m_value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return StringUtil.strcat(m_domain, Seperator, m_value);
    }
}
