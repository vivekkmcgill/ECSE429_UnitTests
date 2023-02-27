Feature: Grouping todos in a category

    As an employee, I want to group my daily tasks into various categories to manage them better

    Background: An existing project is created and can contain a list of TODO tasks
        Given the server is running.
        And TODOs are present with the following fields and categories
            | title                  | doneStatus | description                 | categories |
            | Complete office work   | false      | complete office work        |            |
            | Water plants           | false      | water your plants           |            |
        And categories are present with the following fields
            | title  | description  |  
            | Office |              |
            | Home   |              |

    Scenario Outline: Existing TODOs are associated to a category [Normal]
        When an employee associates a TODO with a category
        Then the corresponding category is added to the todo's categories list
        Examples:
            | title                  | doneStatus | description                 | categories |
            | Complete office work   | false      | complete office work        |    Office  |
            | Water plants           | false      | water your plants           |            |

    Scenario Outline: New TODOs are created and associated to a category [Alternate]
        When an employee creates a task with a project associated to it in the categories array
        Then the corresponding category is added to the todos categories list
        Examples:
            | title                  | doneStatus | description                 | categories |
            | Complete office work   | false      | complete office work        |    Office  |
            | Water plants           | false      | water your plants           |            |
            | Wash dishes            | false      | wash your dishes            |     Home   |

    Scenario Outline: Existing TODOs are associated to a non-existent category [Error]
        When an employee tries to associate a TODO with a non-extsient or deleted category
        Then user receives an error and no associations are added to the todo's categories list
        Examples:
            | title                  | doneStatus | description                 | categories |
            | Complete math homework | false      | complete math homework      |            |
            | Water plants           | false      | water your plants           |            |
