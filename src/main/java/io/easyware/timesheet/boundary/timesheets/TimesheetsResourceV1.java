package io.easyware.timesheet.boundary.timesheets;

import io.easyware.timesheet.entities.TimeRecord;
import lombok.extern.java.Log;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Log
@Tag(name = "Timesheet")
@Path("/v1" + TimesheetsResource.PATH_PREFIX)
public class TimesheetsResourceV1 extends TimesheetsResource {

    @GET
    @Path("/year/{year}/month/{month}/id/{id}")
    public Response get(@PathParam("year") int year, @PathParam("month") int month, @PathParam("id") String id) {
        List<TimeRecord> timeRecords = timeService.getTimeRecordsForMonth(year, month, id);
        if (timeRecords.isEmpty()) return Response.noContent().build();
        return Response.ok(timeService.report(timeRecords)).build();
    }

    @GET
    @Path("/current/{id}")
    public Response getCurrentMonthTimeRecords(@PathParam("id") String id) {
        LocalDate currentDate = LocalDate.now();
        int year = currentDate.getYear();
        int month = currentDate.getMonthValue();
        List<TimeRecord> timeRecords = timeService.getTimeRecordsForMonth(year, month, id);
        if (timeRecords.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(timeService.report(timeRecords)).build();
    }

    @GET
    @Path("/previous/{id}")
    public Response getPreviousMonthTimeRecords(@PathParam("id") String id) {
        LocalDate previousMonthDate = LocalDate.now().minusMonths(1);
        int year = previousMonthDate.getYear();
        int month = previousMonthDate.getMonthValue();
        List<TimeRecord> timeRecords = timeService.getTimeRecordsForMonth(year, month, id);
        if (timeRecords.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(timeService.report(timeRecords)).build();
    }


    @GET
    @Path("/today/{id}")
    public Response get(@PathParam("id") String id) {
        List<TimeRecord> timeRecords = timeService.todaysRecords(id);
        if (timeRecords.isEmpty()) return Response.noContent().build();
        return Response.ok(timeService.report(timeRecords)).build();
    }

    @POST
    @Path("/start/{id}")
    @Transactional
    public Response postStart(@PathParam("id") String id) {
        return Response.status(Response.Status.CREATED).entity(timeService.report(timeService.start(id))).build();
    }

    @POST
    @Path("/end/{id}")
    @Transactional
    public Response postEnd(@PathParam("id") String id) {
        return Response.status(Response.Status.CREATED).entity(timeService.report(timeService.end(id))).build();
    }

    // Update an existing time record
    @PUT
    @Path("/id/{id}")
    @Transactional
    public Response updateTimeRecord(@PathParam("id") String id, TimeRecord timeRecord) {
        Optional<TimeRecord> existingTimeRecord = timeService.findById(id);
        if (existingTimeRecord.isEmpty()) {
            Response.status(404).build();
        }
        TimeRecord tr = existingTimeRecord.get();
        tr.setStart(timeRecord.getStart());
        tr.setEnd(timeRecord.getEnd());
        timeService.save(tr);
        return Response.ok(timeService.report(timeService.todaysRecords(timeRecord.getEmployeeId()))).build();
    }

    // Delete a time record
    @DELETE
    @Path("/id/{id}")
    @Transactional
    public Response deleteTimeRecord(@PathParam("id") String id) {
        Optional<TimeRecord> existingTimeRecord = timeService.findById(id);
        if (existingTimeRecord.isEmpty()) {
            Response.status(404).build();
        }
        TimeRecord tr = existingTimeRecord.get();
        timeService.delete(tr);
        return Response.ok(timeService.report(timeService.todaysRecords(tr.getEmployeeId()))).build();
    }
//
//    @PUT
//    @Wrapper
//    @Path("timesheets/record")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response putTimesheetRecord(
//            String mailAddress) throws InvalidMailAddressException, MailAddressNotFoundException {
//
//        // Validate the email address
//        if (!Validator.isValidMailAddress(mailAddress)) {
//            throw new InvalidMailAddressException(mailAddress);
//        }
//
//        // Check if the email address is valid
//        if (!mailAddress.equalsIgnoreCase("mail@domain.com")) {
//            throw new MailAddressNotFoundException(mailAddress);
//        }
//
//        // Return the mailbox information in JSON format
//        return Response.ok().entity(mailAddress).build();
//    }
}

