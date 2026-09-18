Booking system

### US-001:

As a client, I want to see the list of available hotels for a selected location and date,
so I can choose one.

#### AC:

1. **Given**: I have selected a location and dates on the search page \
   **When**: I search for hotels \
   **Then**: I should the list of hotels with available rooms

2. **Given**: I am searching for hotels in selected location and dates \
   **When**: no hotels are available \
   **Then**: I should see a message that no hotels are available

3. **Given**: I am authenticated as a client \
   **When**: I am viewing the hotel list \
   **Then**: I should the list of hotels with each displaying name, picture, price per
   night, rating

4. **Given**: I am viewing the list of hotels \
   **When**: There are more hotels available that can be fitted on one page \
   **Then**: I should be able to navigate through multiple pages or scroll to see more
   results

5. **Given**: I am viewing the list of hotels \
   **When**: I click on a hotel \
   **Then**: I should be redirected to the hotel's page

6. **Given**: I am viewing the list of hotels \
   **When**: The hotels are displayed \
   **Then**: By default, the hotels should be sorted by rating

### US-002:

As a client, I want to apply filters to the list of hotels so that I narrow down results
to my preferences.

#### AC:

1. **Given**: I am viewing the list of hotels \
   **When**: I set min and/or max price per night \
   **Then**: only hotels within that price range should be displayed

2. **Given**: I am viewing the list of hotels \
   **When**: I set min and/or max rating \
   **Then**: only hotels within that rating range should be displayed

3. **Given**: I am viewing the list of hotels \
   **When**: I select one or more amenities (e.g., Wi-Fi, AC, parking, pet-friendly)
   **Then**: only hotels offering all selected amenities should be displayed (AND logic)

4. **Given**: I am viewing the list of hotels \
   **When**: I apply more than one filter at the same time \
   **Then**: only hotels matching all selected filters should be displayed (AND logic)

5. **Given**: I have selected one or more filters \
   **When**: no hotels match the selected criteria \
   **Then**: I should see a message indicating no results were found

6. **Given**: I have selected one or more filters \
   **When**: I clear all filters \
   **Then**: the full list of available hotels should be restored

7. **Given**: I am viewing the list of hotels \
   **When**: I am selecting filters \
   **Then**: only filters that are applicable to the selected location and dates should be
   displayed

8. **Given**: I am viewing the list of hotels with applied filters \
   **When**: I refresh the page or navigate to another one and return to the list \
   **Then**: Previously applied filters should be preserved

### US-003:

As a client, I want to open a hotel's page from the search results list, so I can see more
details about it before booking.

#### AC:

**Given**: I am viewing the list of hotels \
**When**: I click on a hotel card \
**Then**: I should be redirected to that hotel's page

**Given**: I am on a hotel's page \
**When**: The page loads \
**Then**: I should see the hotel's name, photos, price per night, rating and address

**Given**: I am on a hotel's page \
**When**: I click the back button or navigate back to the list \
**Then**: I should return to the list of hotels I was previously viewing

**Given**: I clicked on a hotel card \
**When**: The hotel's page is loading \
**Then**: I should see a loading indicator until the page is ready

### US-004:

As a client, I want the filters I applied to stay active when I refresh the page or come
back to the results, so I don't have to set them again.

#### AC:

**Given**: I have applied one or more filters to the list of hotels \
**When**: I refresh the page \
**Then**: The same filters should still be applied and the filtered list should be shown

**Given**: I have applied one or more filters \
**When**: I open a hotel's page and then go back to the list \
**Then**: The filters I applied before should still be active

**Given**: I have applied filters during my current session \
**When**: I close the browser and open the site again later \
**Then**: The filters should be reset to default (not saved beyond the session)

**Given**: I have filters applied \
**When**: I click "Clear filters" \
**Then**: All filters should be reset and the full list of hotels should be shown

### US-005:

As a client, I want to see the list of available rooms in a hotel for a selected date,
so I can choose one.

#### AC:

1. **Given**: I have selected a hotel on the search page \
   **When**: I search for rooms \
   **Then**: I should the list of available rooms

2. **Given**: I am searching for rooms of a selected hotel in selected dates \
   **When**: no rooms are available \
   **Then**: I should see a message that no rooms are available\

3. **Given**: I am authenticated as a client \
   **When**: I am viewing the room list \
   **Then**: I should the list of rooms with each displaying name, pictures, price per
   night, number of available rooms of this type

4. **Given**: I am viewing the list of rooms \
   **When**: I click on a room \
   **Then**: I should be redirected to the room's page

5. **Given**: I am viewing the list of rooms \
   **When**: There are more rooms available that can be fitted on one page \
   **Then**: I should be able to navigate through multiple pages or scroll to see more
   results

### US-006:

As a hotel worker, I want to create rooms for a hotel, so clients can book them.

#### AC:

1. **Given**: I am on the "add room" page for a specific hotel \
   **When**: I fill in required fields (name, capacity, price per night, number of such rooms,
   pictures) with valid values and submit \
   **Then**: a new room should be created within that hotel \
   **And**: the room should appear in the hotel's room list \
   **And**: I should see a confirmation message

2. **Given**: I am on the "add room" page for a specific hotel \
   **When**: I submit the form with invalid values (e.g., empty field, negative price, zero or negative capacity, non-numeric input in numeric fields) \
   **Then**: the room should not be created \
   **And**: I should see validation errors indicating which fields are invalid

3. **Given**: I have submitted valid room details \
   **When**: a system or server error occurs during save \
   **Then**: I should see an error message indicating the room could not be created

4. **Given**: I have successfully created a new room \
   **When**: the room is saved \
   **Then**: it should be visible to clients in search results

5. **Given**: a hotel already has a room with a specific room number/identifier \
   **When**: I try to create a new room using the same identifier for that hotel \
   **Then**: the room should not be created


### US-007:

As a hotel worker, I want to view hotel rooms, so I can manage them.

#### AC:

1. **Given**: I am viewing the room list as a hotel worker \
   **When**: I press the "Add room" button \
   **Then**: I should be redirected to the room creation page