package io.easyware.timesheet.boundary.timesheets;

import io.easyware.timesheet.services.TimeService;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
public class TimesheetsResource {

    public static final String PATH_PREFIX = "/timesheets";

    @Inject
    TimeService timeService;
}

