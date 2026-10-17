## Debugging log
Go back to the [Main README](../../README.md).

### Symptoms

Hotel API allowed to create multiple hotels with the identical name.

### Reason

The case wasn't covered neither in [Requirements](../requirements/requirements.md) nor in [User Stories](../requirements/user-stories.md) despite clear business need ( in the majority of jurisdictions hotel name is a trademark protected by law ). Also, the `save` method in [Service](../../spectrum-app/src/main/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelServiceImpl.java) handling Hotel API business logic was not covered by any tests.

### Fix

The bug was fixed by checking for existence of hotel record with same name in database. Two test cases were added as a regression.
See `saveAddsAndReturnsHotelWhenNameIsAvailable` and `saveThrowsAndDoesNotAddHotelWhenNameAlreadyExists` in [corresponding unit test](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelServiceImplTest.java)

### Lesson

This bug is quite small in a sense of changes needed to fix it. It could have been avoided if project had mandatory coverage level requirement in pre-commit as well as corresponding workflow at GitHub. 
Now with two test cases in [HotelServiceImplTest](../../spectrum-app/src/test/java/edu/ukma/ssdm/teamspectrum/logic/hotel/HotelServiceImplTest.java) it will be flagged even when someone from our team breaks business logic when implementing other features.