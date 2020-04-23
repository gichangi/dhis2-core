package org.hisp.dhis.dxf2.events.event;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hisp.dhis.dxf2.events.report.EventRow;
import org.hisp.dhis.organisationunit.OrganisationUnit;

/**
 * @author Morten Olav Hansen <mortenoh@gmail.com>
 */
public interface EventStore
{
    String INSERT_EVENT_SQL = "insert into programstageinstance (" +
    // @formatter:off
        "programstageinstanceid, " +    // 0
        "programinstanceid, " +         // 1
        "programstageid, " +            // 2
        "duedate, " +                   // 3
        "executiondate, " +             // 4
        "organisationunitid, " +        // 5
        "status, " +                    // 6
        "completeddate, " +             // 7
        "uid, " +                       // 8
        "created, " +                   // 9
        "lastupdated, " +               // 10
        "attributeoptioncomboid, " +    // 11
        "storedby, " +                  // 12
        "completedby, " +               // 13
        "deleted, " +                   // 14
        "code, " +                      // 15
        "createdatclient, " +           // 16
        "lastupdatedatclient, " +       // 17
        //"geometry, " +                  // 19
        "assigneduserid) " +            // 18
        // @formatter:on
        "values ( nextval('programstageinstance_sequence'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

    String INSERT_EVENT_NOTE_SQL = "INSERT INTO TRACKEDENTITYCOMMENT (" + "trackedentitycommentid, " + // 0
        "uid, " + // 1
        "commenttext, " + // 2
        "created, " + // 3
        "creator," + // 4
        "lastUpdated" + // 5
        ") " + "values ( nextval('trackedentitycomment_sequence'), ?, ?, ?, ?, ?)";

    String INSERT_EVENT_COMMENT_LINK = "INSERT INTO programstageinstancecomments (" + "programstageinstanceid, "
        + "sort_order, " + "trackedentitycommentid) " + "values (?, ?, ?)";

    List<Event> getEvents( EventSearchParams params, List<OrganisationUnit> organisationUnits,
        Map<String, Set<String>> psdesWithSkipSyncTrue );

    List<Map<String, String>> getEventsGrid( EventSearchParams params, List<OrganisationUnit> organisationUnits );

    List<EventRow> getEventRows( EventSearchParams params, List<OrganisationUnit> organisationUnits );

    int getEventCount( EventSearchParams params, List<OrganisationUnit> organisationUnits );

    /**
     * Delete events matching the List of UIDs. This operation also remove comments
     * connected to the Event.
     *
     * @param uid a List of Events UID
     */
    void delete( List<String> uid );
}
