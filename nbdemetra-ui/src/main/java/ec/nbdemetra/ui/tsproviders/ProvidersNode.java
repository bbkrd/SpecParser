/*
 * Copyright 2013 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.nbdemetra.ui.tsproviders;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import ec.nbdemetra.ui.Config;
import ec.nbdemetra.ui.DemetraUI;
import ec.nbdemetra.ui.nodes.Nodes;
import ec.nbdemetra.ui.interchange.Importable;
import ec.tss.datatransfer.DataSourceTransferSupport;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.IDataSourceListener;
import ec.tss.tsproviders.IDataSourceLoader;
import ec.tss.tsproviders.IDataSourceProvider;
import ec.tss.tsproviders.TsProviders;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * A root node that represents the parent of all providers.
 *
 * @author Philippe Charles
 */
public final class ProvidersNode extends AbstractNode {

    public static final String ACTION_PATH = "ProvidersNode";

    public ProvidersNode() {
        this(new InstanceContent());
    }

    private ProvidersNode(InstanceContent abilities) {
        // 1. Children and lookup
        super(Children.create(new ProvidersChildFactory(), true), new AbstractLookup(abilities));
        // 2. Abilities
        {
//        abilities.add(DemetraUI.getInstance());// IConfigurable
            abilities.add(new ImportableDataSource());
        }
        // 3. Name and display name
    }

    @Override
    public Action[] getActions(boolean context) {
        return Nodes.actionsForPath(ACTION_PATH);
    }

    @Override
    public PasteType getDropType(Transferable t, int action, int index) {
        if (DataSourceTransferSupport.getDefault().canHandle(t)) {
            return new PasteTypeImpl(t);
        }
        return null;
    }

    @Deprecated
    public void paste(DataSource dataSource) {
        Optional<IDataSourceLoader> loader = TsProviders.lookup(IDataSourceLoader.class, dataSource);
        if (loader.isPresent()) {
            loader.get().open(dataSource);
        }
    }

    private static final class ProvidersChildFactory extends ChildFactory.Detachable<Object> implements LookupListener, PropertyChangeListener, IDataSourceListener {

        private final Lookup.Result<IDataSourceProvider> lookupResult;
        private final DemetraUI demetraUI;

        public ProvidersChildFactory() {
            this.lookupResult = Lookup.getDefault().lookupResult(IDataSourceProvider.class);
            this.demetraUI = DemetraUI.getDefault();
        }

        @Override
        protected void addNotify() {
            lookupResult.addLookupListener(this);
            demetraUI.addPropertyChangeListener(this);
            for (IDataSourceProvider o : getProviders()) {
                o.addDataSourceListener(this);
            }
        }

        @Override
        protected void removeNotify() {
            for (IDataSourceProvider o : getProviders()) {
                o.removeDataSourceListener(this);
            }
            demetraUI.removePropertyChangeListener(this);
            lookupResult.removeLookupListener(this);
        }

        @Override
        protected boolean createKeys(List<Object> list) {
            list.addAll(getKeys());
            return true;
        }

        @Override
        protected Node createNodeForKey(Object key) {
            return demetraUI.isShowTsProviderNodes()
                    ? new ProviderNode((IDataSourceProvider) key)
                    : new DataSourceNode((DataSource) key);
        }

        private List<? extends Object> getKeys() {
            return demetraUI.isShowTsProviderNodes()
                    ? getProviders().toSortedList(ON_CLASS_SIMPLENAME)
                    : getProviders().transformAndConcat(TO_DATASOURCE).toSortedList(ON_TO_STRING);
        }

        private FluentIterable<? extends IDataSourceProvider> getProviders() {
            return FluentIterable.from(lookupResult.allInstances())
                    .filter(demetraUI.isShowUnavailableTsProviders() ? ALL : IS_AVAILABLE);
        }

        //<editor-fold defaultstate="collapsed" desc="LookupListener">
        @Override
        public void resultChanged(LookupEvent ev) {
            refresh(true);
            for (IDataSourceProvider o : getProviders()) {
                o.addDataSourceListener(this);
            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="PropertyChangeListener">
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
                case DemetraUI.SHOW_UNAVAILABLE_TSPROVIDER_PROPERTY:
                case DemetraUI.SHOW_TSPROVIDER_NODES_PROPERTY:
                    refresh(true);
                    break;

            }
        }
        //</editor-fold>

        //<editor-fold defaultstate="collapsed" desc="IDataSourceListener">
        @Override
        public void opened(DataSource dataSource) {
            if (!demetraUI.isShowTsProviderNodes()) {
                refresh(true);
            }
        }

        @Override
        public void closed(DataSource dataSource) {
            if (!demetraUI.isShowTsProviderNodes()) {
                refresh(true);
            }
        }

        @Override
        public void changed(DataSource dataSource) {
            if (!demetraUI.isShowTsProviderNodes()) {
                refresh(true);
            }
        }

        @Override
        public void allClosed(String providerName) {
            if (!demetraUI.isShowTsProviderNodes()) {
                refresh(true);
            }
        }
        //</editor-fold>
    }

    private final class ImportableDataSource implements Importable {

        @Override
        public String getDomain() {
            return ProvidersUtil.getDataSourceDomain();
        }

        @Override
        public void importConfig(Config config) throws IllegalArgumentException {
            DataSource dataSource = ProvidersUtil.getDataSource(config);
            Optional<IDataSourceLoader> loader = TsProviders.lookup(IDataSourceLoader.class, dataSource);
            if (loader.isPresent()) {
                loader.get().open(dataSource);
                Optional<Node> node = ProvidersUtil.findNode(dataSource, ProvidersNode.this);
                if (node.isPresent()) {
                    node.get().setDisplayName(config.getName());
                }
            }
        }
    }

    private final class PasteTypeImpl extends PasteType {

        private final Transferable t;

        public PasteTypeImpl(Transferable t) {
            this.t = t;
        }

        @Override
        public Transferable paste() throws IOException {
            Optional<DataSource> dataSource = DataSourceTransferSupport.getDefault().getDataSource(t);
            if (dataSource.isPresent()) {
                Optional<IDataSourceLoader> loader = TsProviders.lookup(IDataSourceLoader.class, dataSource.get());
                if (loader.isPresent()) {
                    loader.get().open(dataSource.get());
                }
            }
            return null;
        }
    }

    private static final Predicate<IDataSourceProvider> ALL = Predicates.alwaysTrue();

    private static final Predicate<IDataSourceProvider> IS_AVAILABLE = new Predicate<IDataSourceProvider>() {
        @Override
        public boolean apply(IDataSourceProvider input) {
            return input.isAvailable();
        }
    };

    private static final Ordering<IDataSourceProvider> ON_CLASS_SIMPLENAME = Ordering.natural().onResultOf(new Function<IDataSourceProvider, String>() {
        @Override
        public String apply(IDataSourceProvider input) {
            return input.getClass().getSimpleName();
        }
    });

    private static final Ordering<DataSource> ON_TO_STRING = Ordering.natural().onResultOf(new Function<DataSource, String>() {
        @Override
        public String apply(DataSource input) {
            return input.toString();
        }
    });

    private static final Function<IDataSourceProvider, List<DataSource>> TO_DATASOURCE = new Function<IDataSourceProvider, List<DataSource>>() {
        @Override
        public List<DataSource> apply(IDataSourceProvider input) {
            return input.getDataSources();
        }
    };

    public static boolean isProvidersNode(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length == 0;
    }
}
