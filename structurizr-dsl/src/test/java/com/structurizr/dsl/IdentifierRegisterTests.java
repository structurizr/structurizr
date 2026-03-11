package com.structurizr.dsl;

import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierRegisterTests extends AbstractTests {

    private final IdentifiersRegister register = new IdentifiersRegister();

    @Test
    void test_validateIdentifierName() {
        new IdentifiersRegister().validateIdentifierName("a");
        new IdentifiersRegister().validateIdentifierName("abc");
        new IdentifiersRegister().validateIdentifierName("ABC");
        new IdentifiersRegister().validateIdentifierName("softwaresystem");
        new IdentifiersRegister().validateIdentifierName("SoftwareSystem");
        new IdentifiersRegister().validateIdentifierName("123456");
        new IdentifiersRegister().validateIdentifierName("_softwareSystem");
        new IdentifiersRegister().validateIdentifierName("SoftwareSystem-1");

        try {
            new IdentifiersRegister().validateIdentifierName("-softwareSystem");
            fail();
        } catch (Exception e) {
            assertEquals("Identifiers cannot start with a - character", e.getMessage());
        }

        try {
            new IdentifiersRegister().validateIdentifierName("SoftwareSystém");
            fail();
        } catch (Exception e) {
            assertEquals("Identifiers can only contain the following characters: a-zA-Z0-9_-", e.getMessage());
        }
    }

    @Test
    void test_register_ThrowsAnException_WhenTheElementHasAlreadyBeenRegisteredWithADifferentIdentifier() {
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System");
        try {
            register.register("a", softwareSystem);
            register.register("x", softwareSystem);
            fail();
        } catch (Exception e) {
            assertEquals("The element is already registered with an identifier of \"a\"", e.getMessage());
        }
    }

    @Test
    void test_register_ThrowsAnException_WhenTheElementHasAlreadyBeenRegisteredWithAnInternalIdentifier() {
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System");
        try {
            register.register("", softwareSystem);
            register.register("x", softwareSystem);
            fail();
        } catch (Exception e) {
            assertEquals("Please assign an identifier to \"SoftwareSystem://Software System\" before using it", e.getMessage());
        }
    }

    @Test
    void test_register_ThrowsAnException_WhenTheElementHasAlreadyBeenRegisteredWithTheSameIdentifierCasedDifferently() {
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System");
        register.register("SoftwareSystem", softwareSystem);
        try {
            register.register("SOFTWARESYSTEM", softwareSystem);
            fail();
        } catch (Exception e) {
            assertEquals("The identifier \"softwaresystem\" is already in use", e.getMessage());
        }
    }

    @Test
    void test_register_ThrowsAnException_WhenAnElementHasAlreadyBeenRegisteredWithTheSameIdentifier() {
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");

        try {
            register.register("a", a);
            register.register("a", b);
            fail();
        } catch (Exception e) {
            assertEquals("The identifier \"a\" is already in use", e.getMessage());
        }
    }

    @Test
    void test_getElement() {
        SoftwareSystem softwareSystem = model.addSoftwareSystem("Software System");
        register.register("SoftwareSystem", softwareSystem);

        assertSame(softwareSystem, register.getElement("SoftwareSystem"));
        assertSame(softwareSystem, register.getElement("softwareSystem"));
        assertSame(softwareSystem, register.getElement("softwaresystem"));
        assertSame(softwareSystem, register.getElement("SOFTWARESYSTEM"));
    }

    @Test
    void test_getRelationships() {
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");
        Relationship rel = a.uses(b, "Uses");
        register.register("Rel", rel);

        assertSame(rel, register.getRelationship("Rel"));
        assertSame(rel, register.getRelationship("rel"));
        assertSame(rel, register.getRelationship("REL"));
    }

    @Test
    void test_register_ThrowsAnException_WhenTheRelationshipHasAlreadyBeenRegisteredWithADifferentIdentifier() {
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");
        Relationship rel = a.uses(b, "Uses");
        try {
            register.register("Rel1", rel);
            register.register("Rel2", rel);
            fail();
        } catch (Exception e) {
            assertEquals("The relationship is already registered with an identifier of \"Rel1\"", e.getMessage());
        }
    }

    @Test
    void test_register_ThrowsAnException_WhenTheRelationshipHasAlreadyBeenRegisteredWithAnInternalIdentifier() {
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");
        Relationship rel = a.uses(b, "Uses");
        try {
            register.register("", rel);
            register.register("Rel", rel);
            fail();
        } catch (Exception e) {
            assertEquals("Please assign an identifier to \"Relationship://SoftwareSystem://A -> SoftwareSystem://B (Uses)\" before using it", e.getMessage());
        }
    }

    @Test
    void test_register_ThrowsAnException_WhenARelationshipHasAlreadyBeenRegisteredWithTheSameIdentifier() {
        SoftwareSystem a = model.addSoftwareSystem("A");
        SoftwareSystem b = model.addSoftwareSystem("B");
        Relationship rel1 = a.uses(b, "Uses 1");
        Relationship rel2 = a.uses(b, "Uses 2");
        try {
            register.register("r", rel1);
            register.register("r", rel2);
            fail();
        } catch (Exception e) {
            assertEquals("The identifier \"r\" is already in use", e.getMessage());
        }
    }

}