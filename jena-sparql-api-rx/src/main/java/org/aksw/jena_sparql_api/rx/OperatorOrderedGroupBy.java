package org.aksw.jena_sparql_api.rx;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.google.common.base.Objects;

import io.reactivex.Flowable;
import io.reactivex.FlowableOperator;
import io.reactivex.FlowableSubscriber;

/**
 * Ordered group by.
 * 
 * The items' group keys are expected to arrive in order, hence only a single accumulator is active at a time.
 * 
 * <pre>{@code
 * 		Flowable<Entry<Integer, List<Integer>>> list = Flowable
 *			.range(0, 10)
 *			.map(i -> Maps.immutableEntry((int)(i / 3), i))
 *			.lift(new OperatorOrderedGroupBy<Entry<Integer, Integer>, Integer, List<Integer>>(Entry::getKey, ArrayList::new, (acc, e) -> acc.add(e.getValue())));
 *
 * }</pre>
 * 
 * @author raven
 *
 * @param <T>
 * @param <K>
 * @param <V>
 */
public final class OperatorOrderedGroupBy<T, K, V>
    implements FlowableOperator<Entry<K, V>, T> {

    protected Function<? super T, ? extends K> getGroupKey;
    protected Supplier<? extends V> accCtor;
    protected BiConsumer<? super V, ? super T> accAdd;
	
	public OperatorOrderedGroupBy(
			Function<? super T, ? extends K> getGroupKey,
			Supplier<? extends V> accCtor,
			BiConsumer<? super V, ? super T> accAdd) {
		super();
		this.getGroupKey = getGroupKey;
		this.accCtor = accCtor;
		this.accAdd = accAdd;
	}

	@Override
	public Subscriber<? super T> apply(Subscriber<? super Entry<K, V>> child) throws Exception {
        return new Op<>(child, getGroupKey, accCtor, accAdd);
	}

    static final class Op<T, K, V> implements FlowableSubscriber<T>, Subscription {
        final Subscriber<? super Entry<K, V>> child;

        protected Subscription s;
        
        protected Function<? super T, ? extends K> getGroupKey;
        protected Supplier<? extends V> accCtor;
        protected BiConsumer<? super V, ? super T> accAdd;
        
        protected K priorKey;
        protected K currentKey;
        
        protected V currentAcc = null;

        
        public Op(Subscriber<? super Entry<K, V>> child, Function<? super T, ? extends K> getGroupKey,
				Supplier<? extends V> accCtor, BiConsumer<? super V, ? super T> accAdd) {
			super();
			this.child = child;
			this.getGroupKey = getGroupKey;
			this.accCtor = accCtor;
			this.accAdd = accAdd;
		}

        @Override
        public void onSubscribe(Subscription s) {
            this.s = s;
            child.onSubscribe(this);
        }

        @Override
        public void onNext(T item) {
        	currentKey = getGroupKey.apply(item);

        	if(currentAcc == null) {
            	// First time init
        		priorKey = currentKey;
        		currentAcc = accCtor.get();
        	} else if(!Objects.equal(priorKey, currentKey)) {
	        		
        		child.onNext(Maps.immutableEntry(priorKey, currentAcc));
        		
        		currentAcc = accCtor.get();
        	}
    		accAdd.accept(currentAcc, item);
    		priorKey = currentKey;
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onComplete() {
        	if(currentAcc != null) {
        		child.onNext(Maps.immutableEntry(priorKey, currentAcc));        		
        	}
        	
            child.onComplete();
        }

        @Override
        public void cancel() {
            s.cancel();
        }

        @Override
        public void request(long n) {
            s.request(n);
        }
    }
}
