package net.tirgan.mex.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class MexWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MexWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
