@api
Feature: Adding todos to a project

    As a project manager, I wish to delegate the parts of my feature development to employees by breaking it down into manageable sub-tasks

    Background: An existing project is created and can contain a list of TODO tasks
        Given the server is running
        And TODOs are present with the following fields
            | title                  | doneStatus | description                 | tasksof |
            | Add submit button      | false      | Add a button to submit form |         |

    Scenario Outline: New TODOs are created and associated to a project [Normal]
        When a project manager creates a task with a project associated to it in the tasksof array
        Then the coresponding project is added to the todo's tasksof list
        Examples:
            | title                  | doneStatus | description                 | tasksof |
            | Add submit button      | false      | Add a button to submit form |         |
            | Add name field         | true       | Add a name field to form    |    1    |

    Scenario Outline: Existing TODOs are associated to a project [Alternate]
        When a project manager associates a TODO with a project
        Then the coresponding project is added to the todo's tasksof list
        Examples:
            | title                  | doneStatus | description                 | tasksof |
            | Add submit button      | false      | Add a button to submit form |    1    |
            | Add name field         | true       | Add a name field to form    |    1    |

    Scenario Outline: Existing TODOs are associated to a non-existent project [Error]
        When a project manager tries to associate a TODO with a non-extsient or deleted project
        Then user receives an error and no associations are added to the todo's tasksof list
        Examples:
            | title                  | doneStatus | description                 | tasksof |
            | Add submit button      | false      | Add a button to submit form |    2    |
            | Add name field         | true       | Add a name field to form    |    2    |
