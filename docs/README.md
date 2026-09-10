# Atlas User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Managing clients

Atlas keeps track of your clients alongside your tasks. Each client has a name,
and may have a phone number and an email address.

Add a client, with the optional details in any order:

```
client add Mary Jane /phone 91234567 /email mary@example.com
```

Expected outcome:

```
Got it. I've added this client:
  [C] Mary Jane (phone: 91234567) (email: mary@example.com)
Now you have 1 client in the list.
```

`client list` shows every client in the order you added them:

```
Here are your clients:
1.[C] Mary Jane (phone: 91234567) (email: mary@example.com)
```

`client find <keyword>` searches names, phone numbers and email addresses, and
accepts several keywords at once:

```
client find mary
client find 9123 mary@example.com
```

`client delete <number>` removes a client, using the number shown by
`client list`:

```
client delete 1
```

Client numbers are independent of task numbers, so deleting a client never
changes how your tasks are numbered. Both are stored in the same data file.

## Feature ABC

// Feature details


## Feature XYZ

// Feature details
