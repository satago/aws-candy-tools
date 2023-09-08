package net.satago.gradle.common

import org.apache.tools.ant.filters.ReplaceTokens

/**
 * Like ReplaceTokens but with a deferred evaluation of the tokens Hashset to allow
 * for generation after dynamic properties like project version have been evaluated.
 * Use it like this:
 * filter(DeferredReplaceTokens, tokenGenerator: { return createHashSetFromProperties() })
 * The tokenGenerator closure will be invoked as late as possible (when the filter runs)
 *
 * See also: https://discuss.gradle.org/t/indirection-lazy-evaluation-for-copy-spec-filter-properties/7173/8
 */
class DeferredReplaceTokens extends FilterReader {
    /**
     * This is the closure that is expected to return something that can be converted to a Hashset
     */
    def tokenGenerator
    FilterReader actualReader

    Reader internalReader

    public DeferredReplaceTokens(Reader reader) {
        super(reader)
        internalReader = reader
    }
    /**
     * On-demand creation of the actual ReplaceToken instance
     * @return The reader we delegate to
     */
    FilterReader reader() {
        if (actualReader == null) {
            actualReader = new ReplaceTokens(internalReader)
            Hashtable tokens = tokenGenerator()
            // setTokens is really private, but all gradle example code
            // use it like it's public so I will as well
            actualReader.tokens = tokens
        }
        return actualReader
    }

    @Override
    int read(char[] cbuf, int off, int len) throws IOException {
        return reader().read(cbuf, off, len)
    }

    @Override
    int read() throws IOException {
        return reader().read()
    }

    @Override
    void close() throws IOException {
        reader().close()
    }
}