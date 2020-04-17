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
import java.util.List;

import org.hisp.dhis.dxf2.importsummary.ImportStatus;
import org.hisp.dhis.dxf2.importsummary.ImportSummary;
import org.hisp.dhis.program.Program;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStatus;
import org.hisp.dhis.trackedentity.TrackedEntityInstance;

/**
 * @author Luciano Fiandesio
 */
public class ProgramInstanceCheck
    implements
    ValidationCheck
{
    @Override
    public ImportSummary check( ImmutableEvent event, ValidationContext ctx )
    {
        Program program = ctx.getProgramsMap().get( event.getProgram() );
        ProgramInstance programInstance = ctx.getProgramInstanceMap().get( event.getUid() );
        TrackedEntityInstance trackedEntityInstance = ctx.getTrackedEntityInstanceMap().get( event.getUid() );

        List<ProgramInstance> programInstances;
        if ( program.isRegistration() )
        {
            if ( programInstance == null ) // Program Instance should be NOT null, after the pre-processing stage
            {
                programInstances = new ArrayList<>(
                    ctx.getProgramInstanceStore().get( trackedEntityInstance, program, ProgramStatus.ACTIVE ) );

                if ( programInstances.isEmpty() )
                {
                    return new ImportSummary( ImportStatus.ERROR, "Tracked entity instance: "
                        + trackedEntityInstance.getUid() + " is not enrolled in program: " + program.getUid() )
                            .setReference( event.getEvent() ).incrementIgnored();
                }
                else if ( programInstances.size() > 1 )
                {
                    return new ImportSummary( ImportStatus.ERROR,
                        "Tracked entity instance: " + trackedEntityInstance.getUid()
                            + " has multiple active enrollments in program: " + program.getUid() )
                                .setReference( event.getEvent() ).incrementIgnored();
                }
            }
        }
        else
        {
            programInstances = ctx.getProgramInstanceStore().get( program, ProgramStatus.ACTIVE );
            if ( programInstances.size() > 1 )
            {
                return new ImportSummary( ImportStatus.ERROR,
                    "Multiple active program instances exists for program: " + program.getUid() )
                        .setReference( event.getEvent() ).incrementIgnored();
            }
        }

        return new ImportSummary();
    }

    @Override
    public boolean isFinal()
    {
        return false;
    }
}
