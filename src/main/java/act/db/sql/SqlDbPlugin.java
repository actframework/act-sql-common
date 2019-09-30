package act.db.sql;

/*-
 * #%L
 * ACT SQL Common Module
 * %%
 * Copyright (C) 2015 - 2018 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.app.App;
import act.app.event.SysEventId;
import act.db.DbPlugin;
import act.db.sql.inject.SqlDbProviders;
import act.db.sql.tx.TxContext;
import act.event.ActEventListenerBase;
import act.handler.event.BeforeResultCommit;
import org.osgl.util.S;

public abstract class SqlDbPlugin extends DbPlugin {

    private static boolean registered;

    @Override
    protected final void applyTo(final App app) {
        if (registered) {
            return;
        }
        registered = true;
        super.applyTo(app);
        app.jobManager().on(SysEventId.STOP, jobId("reset registered"), new Runnable() {
            @Override
            public void run() {
                registered = false;
            }
        });
        app.jobManager().on(SysEventId.PRE_START, jobId("class init and reset TxContext"), new Runnable() {
            @Override
            public void run() {
                SqlDbProviders.classInit(app);
                TxContext.reset();
            }
        });
        app.eventBus().bind(BeforeResultCommit.class, new ActEventListenerBase<BeforeResultCommit>() {
            @Override
            public void on(BeforeResultCommit eventObject) {
                TxContext.clear();
            }
        });
        doExtendedApplyTo(app);
    }

    protected void doExtendedApplyTo(App app) {
    }

    protected String jobId(String task) {
        return S.buffer(getClass().getName()).append(" - ").append(task).toString();
    }
}
