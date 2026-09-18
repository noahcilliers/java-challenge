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




## Requirements
So we must expose some endpoints to each our db. 

Three specified endpoints are getAllEmployees, getEmployeeByUuid, and createEmployee. 


Considerations: 

Some things that are missing could be to edit an existing employee (PUT) or delete and employee (DELETE)... 

I also think we could make a better getEmployee request... The current GET method can only grab all employees or one employee with their specific Uuid.. So maybe we could make another ednpoint to expose more parameters like name, job title, salary, email.

## Design
First, we need to create the Employee class which implements the Employee interface.

Created EmployeeImpl.java which creates the EmployeeImpl clas. We declared all the nvariables for the getters and setters here. One design decision here is to create a variable for full name. Or just combine the first and last name to get the full name. Here we chose to not hold the full name as a variable because we would then have to parse and edit it if we had a change in the first or last name, so it is more simple to get first and last name in the full name field.

We will begin by implementing the three required endpoints. 






Additional endpoint:
Looking at the employee interface we can see that there is a setContractTerminationDate, so we should hand off an endpoint for Employees-R-US to reach this.




