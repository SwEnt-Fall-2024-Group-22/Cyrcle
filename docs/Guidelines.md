# Guidelines

## Git commit

Commit will follow the *advanced* guidelines of
```
type (opt. scope) : description

optional body

optional footers
```
With the example
```
feat(api): add user registration endpoint

Added a new endpoint for user registration, including validation and database persistence.
Closes #56
```
### Types
- `arch` for major architecture modification
- `dev` for small architecture improvement
- `feat` for feature development
- `fix` for fixes
- `track` for time tracking
- `refactor` for code refactor
- `doc` for documentation (both in code and user documentation)
- `cicd` for CI/CD modification
- `misc` for what doesn't have a category

## Inside the code

Android Studio, as it is based on JetBrains IDE, support comment highlights for specified strings 
(tags), such as `TODO`s. Those tags can be specified in ```File > Settings > Editor > TODO```
Please not also that, on the bottom left (by default), under the "Build" tab lays an dotted 
hamburger menu : it can display all the tags that you defined in the "TODO" menu.

Those tags should not appear in the repo (at least on the `main` branch), as they indicate that the
code isn't working.

### TODO
For work that needs to be done, not yet implemented

### FIXME
It doesn't work

### XXX
It works, but I don't know how

### OPTIMIZATION
Something could be optimized

### QUESTION
A question to ask the course staff
