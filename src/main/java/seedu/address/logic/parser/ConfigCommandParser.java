package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULL_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MESSAGE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REGEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SHORT_NAME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.configs.AddPropertyCommand;
import seedu.address.logic.commands.configs.ChangeTagColorCommand;
import seedu.address.logic.commands.configs.ConfigCommand;
import seedu.address.logic.commands.configs.ConfigCommand.ConfigType;
import seedu.address.logic.commands.configs.ImportCalenderCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ConfigCommand object
 */
public class ConfigCommandParser implements Parser<ConfigCommand> {

    /* Regular expressions for validation. ArgumentMultiMap not applicable here. */
    private static final Pattern CONFIG_COMMAND_FORMAT = Pattern.compile("--(?<configType>\\S+)(?<configValue>.+)");
    private static final Pattern TAG_COLOR_FORMAT =
            Pattern.compile("(?<tagName>\\p{Alnum}+)\\s+(?<tagNewColor>#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$)");
    private static final Pattern URL_FORMAT =  Pattern.compile("https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]"
            + "{2,256}\\.[a-z]{2,4}\\b([-a-zA-Z0-9@:%_+.~#?&//=]*)");

    @Override
    public ConfigCommand parse(String args) throws ParseException {
        requireNonNull(args);

        // Defensive programming here to use trim again.
        final Matcher matcher = CONFIG_COMMAND_FORMAT.matcher(args.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ConfigCommand.MESSAGE_USAGE));
        }

        final String configType = matcher.group("configType").trim();
        if (!checkConfigType(configType)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ConfigCommand.MESSAGE_USAGE));
        }

        final ConfigType enumConfigType = toEnumType(configType);
        final String configValue = matcher.group("configValue").trim();

        return checkConfigValue(enumConfigType, configValue);
    }

    private boolean checkConfigType(String type) {
        return ConfigCommand.TO_ENUM_CONFIG_TYPE.containsKey(type);
    }

    private ConfigType toEnumType(String type) {
        return ConfigCommand.TO_ENUM_CONFIG_TYPE.get(type);
    }

    /**
     * Validates the input for different {@link ConfigType} and creates an {@link ConfigCommand} accordingly.
     */
    private ConfigCommand checkConfigValue(ConfigType enumConfigType, String value) throws ParseException {
        switch (enumConfigType) {
        case ADD_PROPERTY:
            return checkAddProperty(value);
        case IMPORT_CALENDAR:
            return checkImportCalendar(value);
        case TAG_COLOR:
            return checkTagColor(value);
        default:
            System.err.println("Unknown ConfigType. Should never come to here.");
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE));
        }
    }

    /**
     * Creates an {@link ChangeTagColorCommand}.
     */
    private ChangeTagColorCommand checkTagColor(String value) throws ParseException {
        final Matcher matcher = TAG_COLOR_FORMAT.matcher(value.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ConfigCommand.MESSAGE_USAGE));
        }

        final String tagName = matcher.group("tagName").trim();
        final String tagColor = matcher.group("tagNewColor").trim();

        return new ChangeTagColorCommand(value, tagName, tagColor);
    }

    /**
     * Creates an {@link AddPropertyCommand}.
     */
    private AddPropertyCommand checkAddProperty(String value) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(value, PREFIX_SHORT_NAME, PREFIX_FULL_NAME, PREFIX_MESSAGE, PREFIX_REGEX);
        if (!ParserUtil.arePrefixesPresent(argMultimap,
                PREFIX_SHORT_NAME, PREFIX_FULL_NAME, PREFIX_MESSAGE, PREFIX_REGEX)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ConfigCommand.MESSAGE_USAGE));
        }

        String shortName = argMultimap.getValue(PREFIX_SHORT_NAME).get();
        String fullName = argMultimap.getValue(PREFIX_FULL_NAME).get();
        String constraintMessage = argMultimap.getValue(PREFIX_MESSAGE).get();
        String regex = argMultimap.getValue(PREFIX_REGEX).get();

        return new AddPropertyCommand(value, shortName, fullName, constraintMessage, regex);
    }

    /**
     * Creates an {@link ImportCalenderCommand}.
     */
    private ImportCalenderCommand checkImportCalendar(String value) throws ParseException {
        final Matcher matcher = URL_FORMAT.matcher(value.trim());
        if (!matcher.matches()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ConfigCommand.MESSAGE_USAGE));
        }

        return new ImportCalenderCommand(value);
    }
}
