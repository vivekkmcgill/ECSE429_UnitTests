Feature: Removing todos from a project

    As a project manager, I want to clear unnecessary or outdated TODOs from a project to remove clutter

    Background: An existing project is created and can contain a list of TODO tasks
        Given the server is running
        And TODOs are present with the following fields
            | title                  | doneStatus | description                 | tasksof         |
            | Add submit button      | true       | Add a button to submit form |    Form         |
            | Design search bar      | true       | Design the search bar       |    Search       |
        And projects are present with the following fields
            | title     | completed  | description                           | active | tasks               |
            | Form      | false      | Add a form to sign up for our service | true   | "Add submit button" |
            | Search    | false      | Add a search bar                      | true   | "Design search bar" |

    Scenario Outline: A TODO is disassociated from a project [Normal]
        When a project manager removes a task from a project associated to it
        Then the coresponding project is removed from the todo's <tasksof> list
        Examples:
            | title                  | doneStatus | description                 | tasksof    |
            | Add submit button      | true       | Add a button to submit form |            |
            | Add name field         | true       | Add a name field to form    |    2       |

    Scenario Outline: Existing TODOs are deleted [Alternate]
        When a project manager deletes a TODO that is associated with a project
        Then the coresponding project no longer has the todo in its <tasks> field
        Examples:
            | title     | completed  | description                           | active | tasks               |
            | Form      | false      | Add a form to sign up for our service | true   | "Add submit button" |
            | Search    | false      | Add a search bar                      | true   |                     |

    Scenario Outline: Todos are removed twice from a project [Error]
        When a project manager tries to remove a todo associated to a project, but forgets that it was already removed earlier
        Then user receives an error and no changes are done to the project's <tasks> list
        Examples:
            | title     | completed  | description                           | active | tasks               |
            | Form      | false      | Add a form to sign up for our service | true   | "Add submit button" |
            | Search    | false      | Add a search bar                      | true   |                     |
