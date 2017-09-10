package act.db.sql.ddl;

/*-
 * #%L
 * ACT AAA Plugin
 * %%
 * Copyright (C) 2015 - 2017 ActFramework
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

import act.db.sql.SqlDbService;
import act.db.sql.SqlDbServiceConfig;
import act.util.DestroyableBase;
import org.osgl.$;

public abstract class DDL extends DestroyableBase {

    protected SqlDbService svc;
    protected SqlDbServiceConfig config;

    public DDL(SqlDbService svc, SqlDbServiceConfig config) {
        this.svc = $.notNull(svc);
        this.config = $.notNull(config);
    }

    public abstract void create();

    public abstract void drop();

    @Override
    protected abstract void releaseResources();

    public static void execute(SqlDbService svc, SqlDbServiceConfig config) {
        new EbeanDDL(svc, config).destroy();
    }

}
