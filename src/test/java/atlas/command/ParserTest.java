package atlas.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import atlas.AtlasException;
import atlas.task.Deadline;
import atlas.task.Event;
import atlas.task.Task;
import atlas.task.Todo;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Tests command recognition and task parsing. */
public class ParserTest {

    @Test
    void parsesKnownCommandsAndRejectsUnknownPrefixes() {
        assertEquals(Command.TODO, Parser.parseCommand("todo read"));
        assertEquals(Command.DEADLINE, Parser.parseCommand("deadline submit /by 2026-09-01"));
        assertEquals(Command.EVENT, Parser.parseCommand("event meeting /from 2pm /to 3pm"));
        assertEquals(Command.FIND, Parser.parseCommand("find book"));
        assertNull(Parser.parseCommand("today"));
        assertNull(Parser.parseCommand("todoist read"));
    }

    @Test
    void parsesFindKeywordAndRejectsBlankKeyword() throws AtlasException {
        assertEquals("book", Parser.parseKeyword("find book", Command.FIND));

        AtlasException exception = assertThrows(AtlasException.class,
                () -> Parser.parseKeyword("find ", Command.FIND));
        assertEquals("What shall I seek, mortal? Use: find <keyword>", exception.getMessage());
    }

    @Test
    void parsesTaskIndexesAndReportsNonNumbersAsInvalid() {
        assertEquals(3, Parser.parseIndex("mark 3", Command.MARK));
        assertEquals(0, Parser.parseIndex("delete 0", Command.DELETE));
        assertEquals(-1, Parser.parseIndex("unmark many", Command.UNMARK));
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
        AtlasException missingBy = assertThrows(AtlasException.class,
                () -> Parser.parseTask("deadline submit", Command.DEADLINE));
        AtlasException invalidDate = assertThrows(AtlasException.class,
                () -> Parser.parseTask("deadline submit /by tomorrow", Command.DEADLINE));
        AtlasException missingTo = assertThrows(AtlasException.class,
                () -> Parser.parseTask("event meeting /from 2pm", Command.EVENT));

        assertEquals("The Fates weave on schedule. Use: deadline <desc> /by <when>", missingBy.getMessage());
        assertEquals("The Fates cannot read that date, mortal. Use: deadline <desc> /by yyyy-mm-dd",
                invalidDate.getMessage());
        assertEquals("Icarus never planned a landing either. Use: event <desc> /from <start> /to <end>",
                missingTo.getMessage());
    }

    @Test
    void rejectsTaskCommandsWithBlankDescriptions() {
        AtlasException exception = assertThrows(AtlasException.class,
                () -> Parser.parseTask("todo  ", Command.TODO));

        assertEquals("Name your labour, mortal: todo <desc>", exception.getMessage());
    }
}
