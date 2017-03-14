package timr.varioous.com.timrapp.timr;

import android.content.Context;
import android.os.AsyncTask;

import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import timr.varioous.com.timrapp.model.FinishedTime;
import timr.varioous.com.timrapp.database.TimeDataSource;

/**
 * Created by holzm on 14-Feb-17.
 */

/**
 * Task to transfer time to timr via thw SOAP-Interface
 */
public class TimrInterfaceTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private static final String AUTHORIZATION = "Basic XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX=";

    public TimrInterfaceTask(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String NAMESPACE = "http://timr.com/timrsync";
        String SOAP_ACTION = "http://timr.com/timrsync/AddWorkItemRequest";
        String METHOD_NAME = "SaveWorkTimeRequest";

        TimeDataSource timeDataSource = new TimeDataSource(this.context);
        timeDataSource.open();
        List<FinishedTime> finishedTimes = timeDataSource.getItemsToSync();
        timeDataSource.close();

        for (FinishedTime f : finishedTimes) {
            boolean success = true;
            try {
                SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                request.addProperty("n0:externalUserId", f.getTimrUserId());
                request.addProperty("n0:startTime", f.getStartTime());
                request.addProperty("n0:endTime", f.getEndTime());
                request.addProperty("n0:externalWorkItemId", "varioous-anwesend");
                request.addProperty("n0:description", "Created by varioous Zeiterfassung Android App");

                SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);

                envelope.dotNet = false;
                envelope.setAddAdornments(false);
                envelope.implicitTypes = false;
                envelope.setOutputSoapObject(request);

                HttpTransportSE androidHttpTransport = new HttpTransportSE("http://timrsync.timr.com/timr/timrsync");
                List<HeaderProperty> headerList = new ArrayList<HeaderProperty>();
                headerList.add(new HeaderProperty("Authorization", TimrInterfaceTask.AUTHORIZATION));
                androidHttpTransport.call(SOAP_ACTION, envelope, headerList);
            } catch (Exception e) {
                success = false;
            }

            if(success) {
                timeDataSource.open();
                timeDataSource.setSuccessfullySyncedItem(f.getId());
                timeDataSource.close();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {

    }
}
