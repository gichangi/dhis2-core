package org.hisp.dhis.dxf2.events.event.validation;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.hisp.dhis.dxf2.common.ImportOptions;
import org.hisp.dhis.dxf2.events.event.Event;
import org.hisp.dhis.dxf2.importsummary.ImportConflict;
import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.security.Authorities;
import org.hisp.dhis.util.DateUtils;

/**
 * @author Luciano Fiandesio
 */
public class EventBaseCheck
    implements
    ValidationCheck
{

    @Override
    public ImportSummary check( Event event, ValidationContext ctx )
    {
        ImportSummary importSummary = new ImportSummary();
        List<String> errors = validate( event, ctx );
        if ( !errors.isEmpty() )
        {
            importSummary.setStatus( ImportStatus.ERROR );
            importSummary.getConflicts()
                .addAll( errors.stream().map( s -> new ImportConflict( "Event", s ) ).collect( Collectors.toList() ) );
            importSummary.incrementIgnored();

            return importSummary;
        }

        return importSummary;
    }

    @Override
    public boolean isFinal()
    {
        return false;
    }

    private List<String> validate( Event event, ValidationContext ctx )
    {
        List<String> errors = new ArrayList<>();

        String programInstanceStatus = get( ctx, event ).getProgramInstanceStatus();
        Date programInstanceEndDate = get( ctx, event ).getProgramInstanceEndDate();
        ImportOptions importOptions = ctx.getImportOptions();

        if ( event.getDueDate() != null && !DateUtils.dateIsValid( event.getDueDate() ) )
        {
            errors.add( "Invalid event due date: " + event.getDueDate() );
        }

        if ( event.getEventDate() != null && !DateUtils.dateIsValid( event.getEventDate() ) )
        {
            errors.add( "Invalid event date: " + event.getEventDate() );
        }

        if ( event.getCreatedAtClient() != null && !DateUtils.dateIsValid( event.getCreatedAtClient() ) )
        {
            errors.add( "Invalid event created at client date: " + event.getCreatedAtClient() );
        }

        if ( event.getLastUpdatedAtClient() != null && !DateUtils.dateIsValid( event.getLastUpdatedAtClient() ) )
        {
            errors.add( "Invalid event last updated at client date: " + event.getLastUpdatedAtClient() );
        }

        if ( programInstanceStatus.equals( ProgramStatus.COMPLETED.name() ) )
        {
            if ( importOptions == null || importOptions.getUser() == null
                || importOptions.getUser().isAuthorized( Authorities.F_EDIT_EXPIRED.getAuthority() ) )
            {
                return errors;
            }

            Date referenceDate = DateUtils.parseDate( event.getCreated() );

            if ( referenceDate == null )
            {
                referenceDate = new Date();
            }

            referenceDate = DateUtils.removeTimeStamp( referenceDate );

            if ( referenceDate.after( DateUtils.removeTimeStamp( programInstanceEndDate ) ) )
            {
                errors.add( "Not possible to add event to a completed enrollment. Event created date ( " + referenceDate
                    + " ) is after enrollment completed date ( " + DateUtils.removeTimeStamp( programInstanceEndDate )
                    + " )." );
            }
        }
        return errors;
    }
}
