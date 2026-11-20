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
package com.hitorro.network.rpc.cluster;

import tools.jackson.databind.*; import tools.jackson.databind.node.*;
import com.hitorro.network.rpc.cluster.IDef; import com.hitorro.jsontypesystem.JVS;
import java.io.*; import java.nio.file.*; import java.util.*;
/**
 * Pins the cluster instance definition JSON (IDef capabilities and ports, which instances share)
 * to what hitorro-base 3.0.2 did on Jackson 2.18, including its quirks (getPortFor throws
 * NumberFormatException for integer ports; pinned, not fixed). hitorro-base has no mappers of
 * its own; everything else Jackson-related is JsonNode passed through.
 * {@code jackson2-base.txt} is the output of the same code run on the 3.0.2 sources
 * (generator: ~/hitorro-j3/tools/Jackson2BaseGolden.java.txt in the migration workspace).
 * Jackson 3 node and exception names are mapped back before comparing (StringNode = TextNode).
 */
class Jackson2FormatGoldenTest {
  static final Map<String, String> out = new LinkedHashMap<>();
  static void p(String k, Object v){ out.put(k, String.valueOf(v).replace("\\","\\\\").replace("\n","\\n").replace("\r","\\r")); }
  static boolean jackson(Throwable t){ return t.getClass().getName().startsWith("com.fasterxml.jackson") || t.getClass().getName().startsWith("tools.jackson"); }
  static String tryIt(java.util.concurrent.Callable<Object> c){ try { return String.valueOf(c.call()); } catch(Throwable t){ return "!"+(t instanceof IOException ? "IOE:" : "RTE:")+(jackson(t) ? "jackson" : t.getClass().getSimpleName()); } }
  static String types(JsonNode n){ StringBuilder t=new StringBuilder(); if (n == null || !n.isObject()) return ""; Iterator<String> it=n.propertyNames().iterator(); while(it.hasNext()){String k=it.next(); t.append(k+":"+n.get(k).getClass().getSimpleName()+",");} return t.toString(); }
  public static Map<String, String> compute(Path dir) throws Exception {
    out.clear();
    ObjectMapper om = com.hitorro.util.json.JsonMappers.json();
    String[][] defs = {
      {"full", "{\"id\":\"n1\",\"server_id\":\"s\",\"ports\":[{\"name\":\"http\",\"port\":8080},{\"name\":\"rpc\",\"port\":\"9090\"},{\"name\":\"dbl\",\"port\":81.9}],\"capabilities\":[{\"capability\":\"index\",\"segment\":\"a\",\"active\":true}],\"started\":1700000000000}"},
      {"empty", "{}"}, {"portsNotArray", "{\"ports\":{\"name\":\"http\",\"port\":1}}"}, {"nullPorts", "{\"ports\":null,\"capabilities\":null}"},
      {"badPort", "{\"ports\":[{\"name\":\"http\",\"port\":\"abc\"},{\"name\":\"t\",\"port\":true},{\"name\":\"big\",\"port\":12345678901}]}"}};
    for (String[] d : defs) {
      for (String proto : new String[]{"http", "rpc", "dbl", "t", "big", "missing"})
        p("idef." + d[0] + ".port." + proto, tryIt(() -> IDef.getPortFor(new JVS(om.readTree(d[1])), proto)));
      p("idef." + d[0] + ".addCapability", tryIt(() -> { JVS j = new JVS(om.readTree(d[1]));
        IDef.addCapability("index", "http://in/\u00e9", "http://ex", "a", false, j);
        IDef.addCapability("search", null, "", "b", true, j);
        IDef.addCapability("search", "u2", "e2", "b", true, j);
        return j.getJsonNode() + " " + types(j.getJsonNode()); }));
    }
    return out;
  }

  static Map<String, String> load(String name) throws IOException {
    Map<String, String> m = new LinkedHashMap<>();
    try (InputStream in = Jackson2FormatGoldenTest.class.getResourceAsStream(name)) {
      if (in == null) return m;
      for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\n")) {
        if (line.isEmpty()) continue;
        int i = line.indexOf('=');
        m.put(line.substring(0, i), line.substring(i + 1));
      }
    }
    return m;
  }

  @org.junit.jupiter.api.Test
  void matchesJackson2Output(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
    Map<String, String> expected = load("jackson2-base.txt");
    
    Map<String, String> actual = new LinkedHashMap<>();
    compute(dir).forEach((k, v) -> actual.put(k, v.replace("StringNode", "TextNode")));
    org.assertj.core.api.Assertions.assertThat(expected).hasSize(35);
    org.assertj.core.api.Assertions.assertThat(actual).containsExactlyEntriesOf(expected);
  }
}

