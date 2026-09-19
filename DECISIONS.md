# Decisions

Fill this in as part of your submission. Brief and specific beats long and
vague — a few sentences per question is plenty. Replace the prompts with your
answers.

## 1. Which endpoints did you add, and why does this system need them?

The additional endpoint that I added is `PUT /api/v1/employee/{uuid}/termination-date`. I surfaced this endpoint to Employees-R-US because they are our new employee management system, so when we have an employee leaving they need some way to set that.

The required endpoints can't do that. The `Employee` interface says a null `contractTerminationDate` means "not terminated", so setting that date is how an employee is terminated in this model, but the three required endpoints can only set it when the employee is created. After that there was no way to record someone leaving. This endpoint closes that gap. It only sets the date. The employee is kept and still returned by both GET endpoints.

It is a PUT because PUT is idempotent. Web hooks retry, so if Employees-R-US sends the same request twice the result is the same, and sending a different date simply corrects the previous one.

## 2. Did you add any dependency? What does it buy, and what does it cost?

I added one dependency: `spring-boot-starter-validation`.

What it buys: the rules are declared as annotations (`@NotBlank`, `@Email`, `@PositiveOrZero`…) on the request classes, right next to the fields they apply to, and `@Valid` on the controller runs them before my code is reached. That keeps the controller and service lean.

What it costs: one more dependency to keep patched, although Spring Boot manages its version so there is no conflict risk. The validation also runs "by magic" before the controller, so it is harder to step through than explicit checks. It also splits validation across two mechanisms: rules that only need the request are annotations, but a rule that needs stored data (a termination date can't be before the stored hire date) still has to be hand-written in `EmployeeService`, so the rules are not all in one place.

## 3. How did you handle errors and logging, and why those mechanisms?

**Errors:**

Every error goes through one class, `GlobalExceptionHandler` (a `@RestControllerAdvice`), so all errors have the same shape and are handled in one place instead of with try/catch in each endpoint. The shape is Spring's built-in `ProblemDetail` (the RFC 7807 standard) rather than an error class of my own, so a consumer can parse our errors with standard tooling and I added no dependency. For validation failures I add an `errors` object that names every invalid attribute at once, so Employees-R-US can fix a bad request in one attempt instead of one error at a time.

The service throws plain exceptions that describe the problem (`EmployeeNotFoundException`, `InvalidTerminationDateException`) and the handler decides the status code (404, 400). I chose this over throwing `ResponseStatusException` from the service, which is what the skeleton did, because it keeps HTTP out of the service layer.

**Logging:**

I used SLF4J through Lombok's `@Slf4j`, both already in the project, rather than `System.out.println`, because it gives levels, timestamps and the class name, and the level can be changed in production by config with no code change. When this misbehaves in production I would want to know three things. What changed and for whom: every create and every termination-date change is logged at INFO with the employee's UUID, and the termination log says whether it replaced an existing date, which shows when a web hook is retrying. What we rejected and why: every rejected request is logged at WARN with the status and cause, plus the names of the invalid attributes for validation failures, so a spike tells me a consumer's integration is broken and what they are sending wrong. And our own bugs: anything unexpected is logged at ERROR with the full stack trace. Reads are DEBUG, off by default, because they are too noisy.

Logs only ever contain UUIDs and attribute names, never values like salary or email. This is enforced two ways: `EmployeeImpl.toString()` only prints the UUID, and a test captures the log output and checks that no personal data appears. Successes are logged in the service and failures in the handler, because a rejected request never reaches the service, so every event is logged exactly once.

## 4. Where the brief left a decision to you, what did you decide?

**What "required" means:**

All seven listed attributes must be present. Text must not be blank (whitespace only is rejected), email must be well formed, salary must be zero or more, age must be positive, and `contractTerminationDate` is optional but cannot be before `contractHireDate`. I was strict about presence but lenient about plausibility (no minimum age or salary cap), because this API is exercised automatically and rejecting a legitimate request is worse than accepting an unusual one.

**Status codes:**

An unknown UUID returns 404: the request was valid, the employee just does not exist. A malformed UUID returns 400: the request itself is wrong. Create returns 201 rather than 200, because a new resource was created.

**`fullName` is derived, not stored:**

`getFullName()` returns first name + last name, so it can never disagree with them if a name changes. The cost is that `setFullName()` is a no-op, because splitting a full name back into two parts would be a guess.

**The request is a separate class from `Employee`:**

`CreateEmployeeRequest` has no `uuid` or `fullName`, so a caller cannot choose their own UUID or overwrite another employee. The UUID is always generated by the server, and a `uuid` sent in the body is ignored.

**Storage and layers:**

Employees live in a `ConcurrentHashMap` keyed by UUID inside `EmployeeRepository`, concurrent because requests are served on many threads at once. `EmployeeService` sits between the controller and the repository and owns the rules (generating the UUID, building the employee, deciding what "not found" means), so the repository only stores and the controller only speaks HTTP. Replacing the map with a real database would touch one class.

**Mock data:**

Three mock employees are created at startup, standing in for the existing employee data, so `GET /api/v1/employee` returns something on a fresh start. They are created through the service so they are built exactly like real ones.

## 5. What did you deliberately choose *not* to do?

**Authentication.** The brief asks for a "protected, secure" API but rules out adding an authentication provider, so I did not add one. What I did within scope: strict input validation, a server-generated UUID so a caller cannot overwrite a record, error responses that never repeat submitted attribute values or leak internals, and logs that never contain personal data (enforced by a test). Authentication is the first thing I would add before this was deployed anywhere real.

**A general update endpoint.** Every attribute would need its own update rules, and nothing in the brief says Employees-R-US edits names or salaries through this API. The termination date was the one gap the model itself showed. I would revisit this if they needed to push other changes, such as a salary change.

**Deleting employees.** The `Employee` contract models leaving as a date, not as removal, and deleting records during a migration risks losing data. I would revisit it for a legal requirement to erase someone's data.

**Search and filtering.** The README defines `GET /api/v1/employee` as unfiltered, and the consumer is a web hook that looks employees up by UUID. I would revisit it for a human-facing consumer, or with pagination once the data no longer fits in one response.

**Duplicate detection.** Two creates with the same email produce two employees. The brief does not say any attribute is unique, and inventing a uniqueness rule could reject legitimate requests. I would add it once Employees-R-US confirms a unique key.

**Clearing a termination date (rehire).** Not required, and it raises a question the brief does not answer: is a rehire the same employee or a new one?
