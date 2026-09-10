package atlas.command;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import atlas.AtlasException;
import atlas.client.Client;
import atlas.task.Deadline;
import atlas.task.Event;
import atlas.task.Task;
import atlas.task.Todo;

/** Tests command recognition, task parsing and client parsing. */
public class ParserTest {

    /** Syntax of the client add command, used in its error messages. */
    private static final String CLIENT_ADD_SYNTAX = "client add <name> [/phone <number>] [/email <address>]";

    @Test
    void parsesKnownCommandsAndRejectsUnknownPrefixes() {
        assertEquals(Command.TODO, Parser.parseCommand("todo read"));
        assertEquals(Command.DEADLINE, Parser.parseCommand("deadline submit /by 2026-09-01"));
        assertEquals(Command.EVENT, Parser.parseCommand("event meeting /from 2pm /to 3pm"));
        assertEquals(Command.FIND, Parser.parseCommand("find book"));
        assertEquals(Command.CLIENT, Parser.parseCommand("client add Bob"));
        assertNull(Parser.parseCommand("today"));
        assertNull(Parser.parseCommand("todoist read"));
    }

    @Test
    void parsesFindKeywordsAndRejectsBlankKeyword() throws AtlasException {
        assertArrayEquals(new String[]{"book"}, Parser.parseKeywords("find book", Command.FIND));
        assertArrayEquals(new String[]{"book", "paper"},
                Parser.parseKeywords("find  book   paper", Command.FIND));

        AtlasException exception = assertThrows(AtlasException.class, () ->
                Parser.parseKeywords("find ", Command.FIND));
        assertEquals("What shall I seek, mortal? Use: find <keyword>", exception.getMessage());
    }

    @Test
    void parsesTaskIndexesAndReportsNonNumbersAsInvalid() {
        assertEquals(3, Parser.parseIndex("mark 3", Command.MARK));
        assertEquals(0, Parser.parseIndex("delete 0", Command.DELETE));
        assertEquals(-1, Parser.parseIndex("unmark many", Command.UNMARK));
        assertEquals(2, Parser.parseIndex("mark 2 ", Command.MARK));
        assertEquals(-1, Parser.parseIndex("mark", Command.MARK));
    }

    @Test
    void parsesTodoCommand() throws AtlasException {
        Task task = Parser.parseTask("todo read the Odyssey", Command.TODO);

        Todo todo = assertInstanceOf(Todo.class, task);
        assertEquals("read the Odyssey", todo.getDescription());
    }

    @Test
    void parsesDeadlineCommand() throws AtlasException {
        Task task = Parser.parseTask("deadline return book /by 2026-09-01", Command.DEADLINE);

        Deadline deadline = assertInstanceOf(Deadline.class, task);
        assertEquals("return book", deadline.getDescription());
        assertEquals(LocalDate.of(2026, 9, 1), deadline.getBy());
    }

    @Test
    void parsesEventCommand() throws AtlasException {
        Task task = Parser.parseTask("event project meeting /from 2pm /to 4pm", Command.EVENT);

        Event event = assertInstanceOf(Event.class, task);
        assertEquals("project meeting", event.getDescription());
        assertEquals("2pm", event.getFrom());
        assertEquals("4pm", event.getTo());
    }

    @Test
    void rejectsMalformedDeadlineAndEventCommands() {
        AtlasException missingBy = assertThrows(AtlasException.class, () ->
                Parser.parseTask("deadline submit", Command.DEADLINE));
        AtlasException invalidDate = assertThrows(AtlasException.class, () ->
                Parser.parseTask("deadline submit /by tomorrow", Command.DEADLINE));
        AtlasException missingTo = assertThrows(AtlasException.class, () ->
                Parser.parseTask("event meeting /from 2pm", Command.EVENT));

        assertEquals("The Fates weave on schedule. Use: deadline <desc> /by <when>", missingBy.getMessage());
        assertEquals("The Fates cannot read that date, mortal. Use: deadline <desc> /by yyyy-mm-dd",
                invalidDate.getMessage());
        assertEquals("Icarus never planned a landing either. Use: event <desc> /from <start> /to <end>",
                missingTo.getMessage());
    }

    @Test
    void rejectsTaskCommandsWithBlankDescriptions() {
        AtlasException exception = assertThrows(AtlasException.class, () ->
                Parser.parseTask("todo  ", Command.TODO));

        assertEquals("Name your labour, mortal: todo <desc>", exception.getMessage());
    }

    @Test
    void parsesClientSubcommandsAndRejectsUnknownOnes() throws AtlasException {
        assertEquals(ClientCommand.ADD, Parser.parseClientSubcommand("client add Bob"));
        assertEquals(ClientCommand.LIST, Parser.parseClientSubcommand("client list"));
        assertEquals(ClientCommand.FIND, Parser.parseClientSubcommand("client find Bob"));
        assertEquals(ClientCommand.DELETE, Parser.parseClientSubcommand("client delete 1"));

        AtlasException bare = assertThrows(AtlasException.class, () ->
                Parser.parseClientSubcommand("client"));
        AtlasException unknown = assertThrows(AtlasException.class, () ->
                Parser.parseClientSubcommand("client foo"));

        assertTrue(bare.getMessage().startsWith("The Oracle is silent on that client command."));
        assertEquals(bare.getMessage(), unknown.getMessage());
    }

    @Test
    void parsesClientAddWithOptionalFieldsInEitherOrder() throws AtlasException {
        Client nameOnly = Parser.parseClient("client add Bob");

        assertEquals("Bob", nameOnly.getName());
        assertEquals("", nameOnly.getPhone());
        assertEquals("", nameOnly.getEmail());

        Client withPhone = Parser.parseClient("client add Mary Jane /phone 91234567");

        assertEquals("Mary Jane", withPhone.getName());
        assertEquals("91234567", withPhone.getPhone());
        assertEquals("", withPhone.getEmail());

        Client reversed = Parser.parseClient("client add Chen Wei /email wei@x.com /phone 8123 4567");

        assertEquals("Chen Wei", reversed.getName());
        assertEquals("8123 4567", reversed.getPhone());
        assertEquals("wei@x.com", reversed.getEmail());
    }

    @Test
    void rejectsClientAddWithoutNameOrWithEmptyMarkerValues() {
        AtlasException noName = assertThrows(AtlasException.class, () ->
                Parser.parseClient("client add"));
        AtlasException markerWithoutName = assertThrows(AtlasException.class, () ->
                Parser.parseClient("client add /phone 91234567"));
        AtlasException noPhone = assertThrows(AtlasException.class, () ->
                Parser.parseClient("client add Bob /phone"));
        AtlasException noEmail = assertThrows(AtlasException.class, () ->
                Parser.parseClient("client add Bob /email"));

        assertEquals("Name your client, mortal: " + CLIENT_ADD_SYNTAX, noName.getMessage());
        assertEquals(noName.getMessage(), markerWithoutName.getMessage());
        assertEquals("A number must follow /phone. Use: " + CLIENT_ADD_SYNTAX, noPhone.getMessage());
        assertEquals("An address must follow /email. Use: " + CLIENT_ADD_SYNTAX, noEmail.getMessage());
    }

    @Test
    void rejectsAdjacentMarkersThatShareOneSpace() throws AtlasException {
        AtlasException noPhone = assertThrows(AtlasException.class, () ->
                Parser.parseClient("client add B /phone /email x@y.com"));
        AtlasException noEmail = assertThrows(AtlasException.class, () ->
                Parser.parseClient("client add A /email /phone 2"));

        assertEquals("A number must follow /phone. Use: " + CLIENT_ADD_SYNTAX, noPhone.getMessage());
        assertEquals("An address must follow /email. Use: " + CLIENT_ADD_SYNTAX, noEmail.getMessage());

        Client separated = Parser.parseClient("client add B /phone 1 /email x@y.com");

        assertEquals("B", separated.getName());
        assertEquals("1", separated.getPhone());
        assertEquals("x@y.com", separated.getEmail());
    }

    @Test
    void parsesClientFindKeywordsAndClientDeleteArguments() throws AtlasException {
        assertArrayEquals(new String[]{"Bob"}, Parser.parseClientKeywords("client find Bob"));
        assertArrayEquals(new String[]{"Bob", "x.com"},
                Parser.parseClientKeywords("client find Bob  x.com"));

        AtlasException blank = assertThrows(AtlasException.class, () ->
                Parser.parseClientKeywords("client find"));
        assertEquals("Whom shall I seek, mortal? Use: client find <keyword>", blank.getMessage());

        assertEquals("2", Parser.parseClientDeleteArgument("client delete 2 "));
        assertEquals("", Parser.parseClientDeleteArgument("client delete"));
        assertEquals(3, Parser.parseNumber(" 3 "));
        assertEquals(-1, Parser.parseNumber("abc"));
    }
}
