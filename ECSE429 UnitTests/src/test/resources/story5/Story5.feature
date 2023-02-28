Feature: Marking tasks of a project as done or not done

    As a project manager, I want to set a done status for tasks so that I can track the progress of the project

    Background: An existing project is created and can contain a list of TODO tasks
        Given the server is running....
        And TODOs are present with the following fields....
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | false      | Add a button to submit form           |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |

    Scenario Outline: A TODO associated to project is marked as done [Normal]
        When a project manager changes the doneStatus for a task to true to mark it as done
        Then the task has its doneStatus field updated to true, and associations remain the same
        Examples:
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | true       | Add a big green button to submit form |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |

    Scenario Outline: A TODO associated to project is marked as done [Alternate]
        When a project manager changes the doneStatus for a task to false to re-open it
        Then the task has its doneStatus field updated to false, and associations remain the same
        Examples:
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | true       | Add a big green button to submit form |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |

    Scenario Outline: An invalid format is set for done status ("completed", "finished", etc.) [Error]
        When a project manager tries to mark a todo named title as done, but provides a non-true/false status
        Then user receives an error and no changes are done to the todo's doneStatus
        Examples:
            | title                  | doneStatus | description                           | tasksof     |
            | Add submit button      | true       | Add a small red button to submit form |    Form     |
            | Design search bar      | true       | Design the search bar                 |    Search   |
