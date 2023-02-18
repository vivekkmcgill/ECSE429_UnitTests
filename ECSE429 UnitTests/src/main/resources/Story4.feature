Feature: Amending names and requirements for tasks in a project

    As a project manager, I want to amend descriptions and names of todos as the project evolves so that they are more accurate/helpful

    Background: An existing project is created and can contain a list of TODO tasks
        Given the server is running
        And TODOs are present with the following fields
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | true       | Add a small red button to submit form |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |
        And projects are present with the following fields
            | title     | completed  | description                           | active | tasks               |
            | Form      | false      | Add a form to sign up for our service | true   | "Add submit button" |
            | Search    | false      | Add a search bar                      | true   | "Design search bar" |

    Scenario Outline: A TODO associated to project has its description changed [Normal]
        When a project manager changes the <description> or requirements for a task
        Then the coresponding task has its <description> field updated, and associations remain the same
        Examples:
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | true       | Add a big green button to submit form |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |

    Scenario Outline: A TODO associated to project has its name changed [Alternate]
        When a project manager changes the <name> for a task
        Then the coresponding task has its <name> field updated, and associations remain the same
        Examples:
            | title                                 | doneStatus | description                           | tasksof     |
            | Add submit button                     | true       | Add a small red button to submit form |    Form     |
            | Design search bar with suggestions    | true       | Design the search bar                 |    Search   |

    Scenario Outline: An invalid name is provided to be set [Error]
        When a project manager tries to amend a todo with an invalid name
        Then user receives an error and no changes are done to the todo's <name>
        Examples:
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | true       | Add a small red button to submit form |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |
