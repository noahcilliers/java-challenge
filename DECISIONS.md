# Decisions

Fill this in as part of your submission. Brief and specific beats long and
vague — a few sentences per question is plenty. Replace the prompts with your
answers.

## 1. Which endpoints did you add, and why does this system need them?

> List the endpoints you added beyond the three required ones, and say what
> problem each one solves for a consumer of this API.

## 2. Did you add any dependency? What does it buy, and what does it cost?

> If you added anything to `build.gradle`, name it and say what you gained and
> what you gave up. If you added nothing, say that — it is a legitimate answer.
>
> Note: Lombok and SLF4J are already on the classpath, so using them is not an
> added dependency.

## 3. How did you handle errors and logging, and why those mechanisms?

> Describe the approach you chose for each, and why you chose it over the
> alternatives. For logging, we are interested in what you would want to know
> when this misbehaves in production.

## 4. Where the brief left a decision to you, what did you decide?

> Not everything here is specified, and some of it is under-specified on
> purpose. Where you had to make a call — about the domain model, the shape of a
> request or response, what to validate — say what you chose and why.

## 5. What did you deliberately choose *not* to do?

> The most useful question here. What did you consider and decide against, and
> what would make you revisit it? Scope you declined on purpose is not a gap.


## Notes

## Requirements
So we must expose some endpoints to each our db. 

Three specified endpoints are getAllEmployees, getEmployeeByUuid, and createEmployee. 


Considerations: 

Some things that are missing could be to edit an existing employee (PUT) or delete and employee (DELETE)... 

I also think we could make a better getEmployee request... The current GET method can only grab all employees or one employee with their specific Uuid.. So maybe we could make another ednpoint to expose more parameters like name, job title, salary, email.

## Design
First, we need to create the Employee class which implements the Employee interface.

Created EmployeeImpl.java which creates the EmployeeImpl clas. We declared all the variables for the getters and setters here. One design decision here is to create a variable for full name. Or just combine the first and last name to get the full name. Here we chose to not hold the full name as a variable because we would then have to parse and edit it if we had a change in the first or last name, so it is more simple to get first and last name in the full name field.

Now we can create a validator to include on our HTTP requests. This will help us ensure that for our endpoints the proper parameters are passed. We could have done this manually, but the Spring Boot starter validator allows us to simplyify our code.

We also have created some unit tests of our own to make sure our code behaves as expected

So now we have our employee class implemented, unit tests proving the class works properly, and our validator set up. So we can now move into the actual http implementation.

For our implementation we need a way to store our employees...
We have created EmployeeRepository.java at  /api/src/main/java/com/challenge/api/repository
- this file allows us to abstract the contact with the map:  mapping our employee id to the proper employee objects

We also need a way to access this map which leads us to the employee service...
We have created EmployeeService.java at  /api/src/main/java/com/challenge/api/service
- this is our controllers access to the repository
- this also creates our UUIDs, builds our employee objects, and decides unknown UUID means "Not Found"

Design decisions here:
- POST returns 201 Created
- Unknown UUID returns 404
- Malformed UUID returns 400

Next we added error handling and logging...

We created GlobalExceptionHandler.java so every error goes through one place and comes back in the same standard format (ProblemDetail, which is built into Spring), and a 400 tells the caller exactly which attributes were wrong. 

For logging we used SLF4J through Lombok's @Slf4j since it is already in the project: we log creates at INFO, rejected requests at WARN and unexpected errors at ERROR, and we only ever log UUIDs and attribute names, never personal data like salary or email.


Additional endpoint:

The existing three endpoints are getting employee information or creating an employee.. This works well, but we aren't surfacing any way to update employees after creation. What specific endpoint would Employees R US be looking to change? Since they are our new employee management system they need a way to "terminate" employees. Since the employees live in our system and we leave the termination to another service we can just allow them to update (PUT) the termination date for an Employee

The endpoint is PUT /api/v1/employee/{uuid}/termination-date with a body of { "contractTerminationDate": "..." }, and it returns 200 with the updated employee. We chose PUT because it is idempotent: web hooks retry, so if Employees-R-US sends the same request twice the result is the same, and sending a different date just replaces the old one (a correction). The employee is never removed, we only set the field, because the Employee interface itself says a null termination date means "not terminated".

The date is required in the body instead of defaulting to now, since the caller knows the real date and it could be backdated or in the future. A date before the hire date returns 400, but unlike on create this check lives in EmployeeService (InvalidTerminationDateException) because the hire date is in our store and not in the request. Unknown UUID is 404 and a malformed UUID is 400, the same as the GET endpoint.



