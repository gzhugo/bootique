package io.bootique.help.config;

import io.bootique.Bootique;
import io.bootique.help.ConsoleAppender;
import io.bootique.meta.config.ConfigListMetadata;
import io.bootique.meta.config.ConfigMapMetadata;
import io.bootique.meta.config.ConfigMetadataNode;
import io.bootique.meta.config.ConfigObjectMetadata;
import io.bootique.meta.config.ConfigValueMetadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ConfigSectionGeneratorTest {

    private static void assertLines(ConfigMetadataNode node, String... expectedLines) {

        StringBuilder expected = new StringBuilder();
        for (String s : expectedLines) {
            expected.append(s).append(DefaultConfigHelpGeneratorTest.NEWLINE);
        }

        StringBuilder buffer = new StringBuilder();
        ConsoleAppender out = new ConsoleAppender(buffer, 80);
        node.accept(new ConfigSectionGenerator(out));
        String help = buffer.toString();
        assertNotNull(help);
        assertEquals(expected.toString(), help);
    }

    @Test
    public void testVisitObjectConfig() {

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigValueMetadata.builder("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigValueMetadata.builder("p1").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p0").type(Boolean.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Bootique.class).build())
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: boolean",
                "      p0: <true|false>",
                "",
                "      # Type: String",
                "      p1: <string>",
                "",
                "      # Type: int",
                "      # Designates an integer value",
                "      p2: <int>",
                "",
                "      # Type: io.bootique.Bootique",
                "      p3: <value>"
        );
    }

    @Test
    public void testVisitListOfValues() {

        ConfigValueMetadata listMd1 = ConfigValueMetadata.builder().type(Integer.TYPE).build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigListMetadata.builder("p1").elementType(listMd1).build())
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: List",
                "      p1:",
                "            - # Element type: int",
                "              <int>"
        );
    }

    @Test
    public void testVisitListOfObjects() {

        ConfigObjectMetadata listMd2 = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigListMetadata.builder("p2").elementType(listMd2).description("I am a list").build())
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: List",
                "      # I am a list",
                "      p2:",
                "            - # Element type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "                  # Type: boolean",
                "                  p3: <true|false>",
                "",
                "                  # Type: String",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitMapOfValues() {

        ConfigValueMetadata mapMd = ConfigValueMetadata.builder().type(Integer.TYPE).build();

        ConfigObjectMetadata rootMd = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigMapMetadata.builder("p1").keysType(Integer.class).valuesType(mapMd).build())
                .build();

        assertLines(rootMd,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: Map",
                "      p1:",
                "            # Keys type: int",
                "            # Values type: int",
                "            <int>: <int>"
        );
    }

    @Test
    public void testVisitObjectWithMapOfObjects() {

        ConfigObjectMetadata mapMd = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigObjectMetadata rootMd = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .addProperty(ConfigMapMetadata.builder("p1").keysType(String.class).valuesType(mapMd).build())
                .build();

        assertLines(rootMd,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Type: Map",
                "      p1:",
                "            # Keys type: String",
                "            # Values type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "            <string>:",
                "                  # Type: boolean",
                "                  p3: <true|false>",
                "",
                "                  # Type: String",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitMapOfMapsOfObjects() {

        ConfigObjectMetadata objectMd = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigMapMetadata subMapMd = ConfigMapMetadata
                .builder()
                .description("Submap description")
                .keysType(String.class)
                .valuesType(objectMd)
                .build();

        ConfigMapMetadata rootMapMd = ConfigMapMetadata
                .builder("root")
                .description("Root map")
                .keysType(String.class)
                .valuesType(subMapMd)
                .build();

        assertLines(rootMapMd,
                "# Type: Map",
                "# Root map",
                "root:",
                "      # Keys type: String",
                "      # Values type: Map",
                "      # Submap description",
                "      <string>:",
                "            # Keys type: String",
                "            # Values type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "            <string>:",
                "                  # Type: boolean",
                "                  p3: <true|false>",
                "",
                "                  # Type: String",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitMapOfListsOfObjects() {

        ConfigObjectMetadata objectMd = ConfigObjectMetadata.builder()
                .type(ConfigRoot2.class)
                .addProperty(ConfigValueMetadata.builder("p4").type(String.class).build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Boolean.TYPE).build())
                .build();

        ConfigListMetadata subListMd = ConfigListMetadata
                .builder()
                .description("Sublist description")
                .elementType(objectMd)
                .build();

        ConfigMapMetadata rootMapMd = ConfigMapMetadata
                .builder("root")
                .description("Root map")
                .keysType(String.class)
                .valuesType(subListMd)
                .build();

        assertLines(rootMapMd,
                "# Type: Map",
                "# Root map",
                "root:",
                "      # Keys type: String",
                "      # Values type: List",
                "      # Sublist description",
                "      <string>:",
                "            - # Element type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot2",
                "                  # Type: boolean",
                "                  p3: <true|false>",
                "",
                "                  # Type: String",
                "                  p4: <string>"
        );
    }

    @Test
    public void testVisitObjectConfig_Inheritance() {

        ConfigObjectMetadata sub1 = ConfigObjectMetadata.builder()
                .type(Config3.class)
                .typeLabel("c3")
                .addProperty(ConfigValueMetadata.builder("p0").type(Boolean.class).build())
                .addProperty(ConfigValueMetadata.builder("p1").type(String.class).build())
                .build();

        ConfigObjectMetadata sub2 = ConfigObjectMetadata.builder()
                .type(Config4.class)
                .typeLabel("c4")
                .addProperty(ConfigValueMetadata.builder("p2").type(Integer.TYPE).description("Designates an integer value").build())
                .addProperty(ConfigValueMetadata.builder("p3").type(Bootique.class).build())
                .build();

        ConfigObjectMetadata m1Config = ConfigObjectMetadata
                .builder("m1root")
                .description("Root config of M1")
                .type(ConfigRoot1.class)
                .abstractType(true)
                .addProperty(ConfigValueMetadata.builder("pa1").type(Integer.TYPE).build())
                .addSubConfig(sub1)
                .addSubConfig(sub2)
                .build();

        assertLines(m1Config,
                "# Type: io.bootique.help.config.ConfigSectionGeneratorTest$ConfigRoot1",
                "# Root config of M1",
                "m1root:",
                "      # Designator of subtype: io.bootique.help.config.ConfigSectionGeneratorTest$Config3",
                "      type: c3",
                "",
                "      # Type: boolean",
                "      p0: <true|false>",
                "",
                "      # Type: String",
                "      p1: <string>",
                "",
                "      # Designator of subtype: io.bootique.help.config.ConfigSectionGeneratorTest$Config4",
                "      type: c4",
                "",
                "      # Type: int",
                "      # Designates an integer value",
                "      p2: <int>",
                "",
                "      # Type: io.bootique.Bootique",
                "      p3: <value>"
        );
    }

    public static class ConfigRoot1 {

    }

    public static class ConfigRoot2 {

    }

    public static class Config3 {

    }

    public static class Config4 {

    }
}
