/**
 * Copyright (c) 2019 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package com.faa.chain.net;

import com.faa.chain.core.Block;

public interface BlockchainListener {

    /**
     * Callback when a new block was added.
     * 
     * @param block
     */
    void onBlockAdded(Block block);
}
